package de.atos.solumversion.services;

import de.atos.solumversion.components.MfcSolutionDescriptorParserFactory;
import de.atos.solumversion.components.MfcSolutionDescriptorParserFactoryException;
import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import de.atos.solumversion.domain.MfcProjectDescriptor;
import de.atos.solumversion.domain.MfcSolutionDescriptor;
import de.atos.solumversion.dto.MfcProjectDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc2.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MfcProjectServiceImpl implements MfcProjectService {

    List<String> projectExtensions = new ArrayList<>(Arrays.asList(".sln", "vcxproj"));

    private SvnService svnService;

    private WorkingCopyDirectoryConfig workingCopyDirectoryConfig;

    private MfcSolutionDescriptorParserFactory mfcSolutionDescriptorParserFactory;

    public MfcProjectServiceImpl(SvnService svnService, WorkingCopyDirectoryConfig workingCopyDirectoryConfig, MfcSolutionDescriptorParserFactory mfcSolutionDescriptorParserFactory) {
        this.svnService = svnService;
        this.workingCopyDirectoryConfig = workingCopyDirectoryConfig;
        this.mfcSolutionDescriptorParserFactory = mfcSolutionDescriptorParserFactory;
    }

    @Override
    public MfcProjectDTO init(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException {

        String workingCopyRootPath = workingCopyDirectoryConfig.getRootDirectory();

        String remoteUrl = mfcProjectDTO.getSvnInfo().getUrl();

        SVNURL svnurl = getSvnUrl(remoteUrl);

        // Get project root structure
        List<SVNDirEntry> rootStructure = doSvnList(SvnTarget.fromURL(svnurl), SVNDepth.IMMEDIATES, SVNRevision.HEAD);
        if(Objects.isNull(rootStructure)){
            throw new MfcProjectServiceException("Root structure has no entries for remote project [%s], remoteUrl");
        }

        // Search root entry
        SVNDirEntry rootEntry = getSolutionRootEntry(rootStructure);

        String solutionName = FilenameUtils.getName(rootEntry.getRepositoryRoot().getPath());
        File projectWorkingCopyRoot = Paths.get(workingCopyRootPath, solutionName).toFile();

        // Checkout root
        doSvnCheckout(SvnTarget.fromFile(projectWorkingCopyRoot), SvnTarget.fromURL(svnurl), SVNDepth.EMPTY, SVNRevision.HEAD);

        // Search solution descriptor
        SVNDirEntry solutionDescriptor = getSolutionDescriptorEntry(rootStructure);

        String projectSolutionRelativeToRootPath = relativize(solutionDescriptor.getRepositoryRoot().toString(), solutionDescriptor.getURL().toString());
        File projectSolutionWorkingCopyFile = Paths.get(projectWorkingCopyRoot.getAbsolutePath(), projectSolutionRelativeToRootPath).toFile();

        // Fetch solution descriptor
        doSvnUpdate(
                new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromFile(projectSolutionWorkingCopyFile))),
                SVNDepth.FILES,
                SVNRevision.HEAD,
                true);


        // Parse solution desciptor
        MfcSolutionDescriptorParser mfcSolutionDescriptorParser = null;
        try {
            mfcSolutionDescriptorParser = mfcSolutionDescriptorParserFactory.create(projectSolutionWorkingCopyFile);
        } catch (MfcSolutionDescriptorParserFactoryException e) {
            throw new MfcProjectServiceException(e.getMessage());
        }

        MfcSolutionDescriptor mfcSolutionDescriptor = mfcSolutionDescriptorParser.parseDescriptor(projectSolutionWorkingCopyFile);

        // Checkout projects descriptors
        Map<String, String> projects = mfcSolutionDescriptor.getProjects();
        List<SvnTarget> projectsDescriptorsTargets = projects.entrySet()
                .stream()
                .filter(stringStringEntry -> stringStringEntry.getValue().endsWith("vcxproj"))
                .map(stringStringEntry -> SvnTarget.fromFile(Paths.get(projectWorkingCopyRoot.getAbsolutePath(), stringStringEntry.getValue()).toFile()))
                .collect(Collectors.toList());

        doSvnUpdate(projectsDescriptorsTargets, SVNDepth.FILES, SVNRevision.HEAD, true);

        MfcProjectDescriptorParser projectDescriptorParser = mfcSolutionDescriptorParser.createProjectDescriptorParser();

        // Parse all project descriptors
        List<MfcProjectDescriptor> mfcProjectDescriptors = new ArrayList<>();
        for(var projectDescriptorTarget : projectsDescriptorsTargets){
            File file = projectDescriptorTarget.getFile();
            MfcProjectDescriptor mfcProjectDescriptor = projectDescriptorParser.parse(file);
            mfcProjectDescriptors.add(mfcProjectDescriptor);
        }

        // Filter for applications or dynamic libraries only
        List<MfcProjectDescriptor> mfcProjectsAllowed = mfcProjectDescriptors
                .stream()
                .filter(mfcProjectDescriptor -> Objects.nonNull(mfcProjectDescriptor.getResourceFileAbsolutePath()))
                .filter(mfcProjectDescriptor -> {
                    MfcProjectDescriptor.Configuration confRel = mfcProjectDescriptor.getConfigurationRelease();
                    if (Objects.nonNull(confRel)) {
                        if (confRel.isApplication() || confRel.isDynamicLibrary()) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                    return false;
                })
                .collect(Collectors.toList());

        // Search sources folder
        SVNDirEntry projectSource = getSrcEntry(rootStructure);
        List<SVNDirEntry> sourceEntryItems = doSvnList(SvnTarget.fromURL(projectSource.getURL()), SVNDepth.INFINITY, SVNRevision.HEAD);

        // Filter to get only resource files
        List<SVNDirEntry> resources = sourceEntryItems
                .stream()
                .filter(svnDirEntry -> svnDirEntry.getName().endsWith(".rc"))
                .filter(svnDirEntry -> {
                    String name = svnDirEntry.getName();
                    Optional<MfcProjectDescriptor> any = mfcProjectsAllowed
                            .stream()
                            .filter(mfcProjectDescriptor -> FilenameUtils.getName(mfcProjectDescriptor.getResourceFileAbsolutePath()).equalsIgnoreCase(name)).findAny();
                    return any.isPresent();
                })
                .collect(Collectors.toList());

        if(resources.isEmpty()){
            throw new MfcProjectServiceException("Missing resource files");
        }

        // Map resources to working copy
        List<SvnTarget> resourcesWC = resources
                .stream()
                .map(svnurl1 -> {
                    String relative = relativize(svnurl1.getRepositoryRoot().toString(), svnurl1.getURL().toString());
                    return SvnTarget.fromFile(Paths.get(projectWorkingCopyRoot.getAbsolutePath(), relative).toFile());
                })
                .collect(Collectors.toList());

        doSvnUpdate(resourcesWC, SVNDepth.FILES, SVNRevision.HEAD, true);

        MfcProjectDTO resultDto = new MfcProjectDTO();
        resultDto.getSvnInfo().setUrl(rootEntry.getRepositoryRoot().toString());
        resultDto.getSvnInfo().setRevision(rootEntry.getRevision());
        resultDto.getSvnInfo().setRevision(rootEntry.getRevision());
        resultDto.getSvnInfo().setHasLocalModifications(false);
        resultDto.getSvnInfo().setLastCommitAuthor(rootEntry.getAuthor());

        resultDto.setName(solutionName);
        resultDto.setVisualStudioVersion(mfcSolutionDescriptor.getVisualStudioVersion());


        return resultDto;
    }



    @Override
    public List<MfcProjectDTO> getProjects() throws MfcProjectServiceException {
        String workingCopyRootDirectory = workingCopyDirectoryConfig.getRootDirectory();
        File workingCopyRootDirectoryFile = new File(workingCopyRootDirectory);

        String[] wcProjects = workingCopyRootDirectoryFile.list((dir, name) -> new File(dir, name).isDirectory());

        List<MfcProjectDTO> mfcProjectDTOS = new ArrayList<>();
        for(var wcProject : wcProjects){

            File wcProjectFile = Paths.get(workingCopyRootDirectory, wcProject).toFile();
            List<SvnStatus> statusListLocalList = null;
            try {
                statusListLocalList = svnService.status(
                        SvnTarget.fromFile(wcProjectFile),
                        SVNDepth.EMPTY,
                        SVNRevision.HEAD,
                        false,
                        true);
            } catch (SVNException e) {
                throw new MfcProjectServiceException(String.format("Error getting status: [%s]", e.getMessage()));
            }

            if(Objects.isNull(statusListLocalList)){
                continue;
            }

            SvnStatus svnStatusLocal = statusListLocalList.get(0);

            List<SvnStatus> statusListRemoteList = null;
            try {
                statusListRemoteList = svnService.status(
                        SvnTarget.fromFile(wcProjectFile),
                        SVNDepth.EMPTY,
                        SVNRevision.HEAD,
                        false,
                        true);
            } catch (SVNException e) {
                throw new MfcProjectServiceException(String.format("Error getting status: [%s]", e.getMessage()));
            }

            if(Objects.isNull(statusListRemoteList)){
                continue;
            }

            SvnStatus statusListRemote = statusListRemoteList.get(0);

            MfcProjectDTO mfcProjectDTO = new MfcProjectDTO();
            mfcProjectDTO.getSvnInfo().setUrl(statusListRemote.getRepositoryRootUrl().toString());
            mfcProjectDTO.getSvnInfo().setRevision(svnStatusLocal.getRevision());

            boolean isOutdated = svnStatusLocal.getRevision() < statusListRemote.getRevision();
            mfcProjectDTO.getSvnInfo().setOutdated(isOutdated);

            // Parse solution descriptor
            String[] solutionDescriptors = wcProjectFile.list((dir, name) -> projectExtensions.stream().anyMatch(s -> name.endsWith(s)));
            if(solutionDescriptors.length == 0){
                throw new MfcProjectServiceException(String.format("Missing solution descriptor for project [%s]", wcProject));
            }

            File solutionDescriptor = Paths.get(wcProjectFile.getAbsolutePath(), solutionDescriptors[0]).toFile();
            MfcSolutionDescriptorParser mfcSolutionDescriptorParser = null;
            try {
                mfcSolutionDescriptorParser = mfcSolutionDescriptorParserFactory.create(solutionDescriptor);
            } catch (MfcSolutionDescriptorParserFactoryException e) {
                throw new MfcProjectServiceException(e.getMessage());
            }

            mfcProjectDTO.setName(wcProject);

            MfcSolutionDescriptor mfcSolutionDescriptor = mfcSolutionDescriptorParser.parseDescriptor(solutionDescriptor);
            mfcProjectDTO.setVisualStudioVersion(mfcSolutionDescriptor.getVisualStudioVersion());

            List<SvnStatus> svnStatuses = doSvnStatus(SvnTarget.fromFile(wcProjectFile), SVNDepth.INFINITY, SVNRevision.HEAD, false, false);
            Optional<SvnStatus> any = svnStatuses
                    .stream()
                    .filter(svnStatus -> !svnStatus.getNodeStatus().equals(SVNStatusType.STATUS_NONE)
                            && !svnStatus.getNodeStatus().equals(SVNStatusType.STATUS_NORMAL)
                            && !svnStatus.getNodeStatus().equals(SVNStatusType.STATUS_IGNORED))
                    .findAny();

            if(any.isPresent()){
                mfcProjectDTO.getSvnInfo().setHasLocalModifications(true);
            }

            mfcProjectDTOS.add(mfcProjectDTO);
        }

        return mfcProjectDTOS;
    }

    @Override
    public void remove(MfcProjectDTO mfcProjectDTO) {
        String workingCopyRootDirectory = workingCopyDirectoryConfig.getRootDirectory();
        String path = workingCopyRootDirectory + "/" + mfcProjectDTO.getName();

        File file = new File(path);
        file.delete();
    }

    // Helpers //

    private SVNDirEntry getSrcEntry(List<SVNDirEntry> rootStructure) throws MfcProjectServiceException {
        List<SVNDirEntry> projectSourcesList = rootStructure.stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().compareToIgnoreCase("src") == 0)
                .collect(Collectors.toList());

        if(projectSourcesList.isEmpty()){
            throw new MfcProjectServiceException("Missing project sources");
        } else if(projectSourcesList.size() > 1){
            throw new MfcProjectServiceException("Found more than one project sources!");
        }

        SVNDirEntry projectSources = projectSourcesList.get(0);
        return projectSources;
    }

    private void doSvnUpdate(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, boolean makeParents) throws MfcProjectServiceException {
        try {
            svnService.update(targets, depth, revision, makeParents);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error SVN update, targets: [%s], error: [%s]", targets.toString(), e.getMessage()));
        }
    }

    private SVNDirEntry getSolutionDescriptorEntry(List<SVNDirEntry> rootStructure) throws MfcProjectServiceException {

        List<SVNDirEntry> projectDescriptors = rootStructure.stream()
                .filter(svnTargetSVNDirEntryEntry -> {
                    return projectExtensions.stream().anyMatch(s -> svnTargetSVNDirEntryEntry.getName().endsWith(s));
                })
                .collect(Collectors.toList());

        if(projectDescriptors.isEmpty()){
            throw new MfcProjectServiceException("Missing project descriptor");
        } else if(projectDescriptors.size() > 1){
            throw new MfcProjectServiceException("Found more than one project descriptor!");
        }

        SVNDirEntry projectDescriptor = projectDescriptors.get(0);
        return projectDescriptor;
    }

    private SVNDirEntry getSolutionRootEntry(List<SVNDirEntry> rootStructure) throws MfcProjectServiceException {
        List<SVNDirEntry> rootEntries = rootStructure.stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().isEmpty())
                .collect(Collectors.toList());

        if(rootEntries.isEmpty()){
            throw new MfcProjectServiceException("Missing project root entry");
        } else if(rootEntries.size() > 1){
            throw new MfcProjectServiceException("Found more than one project root entry!");
        }

        SVNDirEntry rootEntry = rootEntries.get(0);
        return rootEntry;
    }

    private void doSvnCheckout(SvnTarget target, SvnTarget source, SVNDepth depth, SVNRevision revision) throws MfcProjectServiceException {
        try {
            svnService.checkout(target, source, depth, revision);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error SVN checkout, source: [%s], target: [%s], error: [%s]", source.toString(), target.toString(), e.getMessage()));
        }
    }

    private List<SVNDirEntry> doSvnList(SvnTarget target, SVNDepth depth, SVNRevision revision) throws MfcProjectServiceException {
        List<SVNDirEntry> rootStructureList = null;
        try {
            rootStructureList = svnService.list(target, depth, revision);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error SVN list, target: [%s], error: [%s]", target.toString(), e.getMessage()));
        }

        return rootStructureList;
    }

    private List<SvnStatus> doSvnStatus(SvnTarget target, SVNDepth depth, SVNRevision revision, boolean remote, boolean reportAll) throws MfcProjectServiceException {
        List<SvnStatus> statusListLocalList = null;
        try {
            statusListLocalList = svnService.status(target, depth, revision, remote, reportAll);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error SVN status, target: [%s], error: [%s]", target.toString(), e.getMessage()));
        }

        return statusListLocalList;
    }

    private SVNURL getSvnUrl(String remoteUrl) throws MfcProjectServiceException {
        // Create SVN url
        SVNURL svnurl = null;
        try {
            svnurl = SVNURL.parseURIEncoded(remoteUrl);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Cannot parse URL: %s", remoteUrl));
        }
        return svnurl;
    }

    private String relativize(String base, String path){
        return new File(base).toURI().relativize(new File(path).toURI()).getPath();
    }
}
