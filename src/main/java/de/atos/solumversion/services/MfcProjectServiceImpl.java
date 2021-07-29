package de.atos.solumversion.services;

import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import de.atos.solumversion.domain.MfcResourceProperties;
import de.atos.solumversion.domain.SvnInfo;
import de.atos.solumversion.dto.MfcProjectDTO;
import de.atos.solumversion.dto.MfcResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MfcProjectServiceImpl implements MfcProjectService {

    private SvnService svnService;

    private WorkingCopyDirectoryConfig workingCopyDirectoryConfig;

    public MfcProjectServiceImpl(SvnService svnService, WorkingCopyDirectoryConfig workingCopyDirectoryConfig) {
        this.svnService = svnService;
        this.workingCopyDirectoryConfig = workingCopyDirectoryConfig;
    }

    @Override
    public MfcProjectDTO init(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException {
        String remotePath = mfcProjectDTO.getSvnInfo().getUrl();

        // Create SVN url
        SVNURL svnurl = null;
        try {
            svnurl = SVNURL.parseURIEncoded(remotePath);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Cannot parse URL: %s", remotePath));
        }

        // Get project root structure
        List<SVNDirEntry> rootStructureList = null;
        try {
            rootStructureList = svnService.list(
                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromURL(svnurl))),
                    SVNDepth.IMMEDIATES,
                    SVNRevision.HEAD);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error loading root project structure for [%s]: %s", remotePath, e.getMessage()));
        }

        if(Objects.isNull(rootStructureList)){

        }

        // Search root entry
        List<SVNDirEntry> rootEntries = rootStructureList.stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().isEmpty())
                .collect(Collectors.toList());

        if(rootEntries.isEmpty()){
            throw new MfcProjectServiceException("Missing project root entry");
        } else if(rootEntries.size() > 1){
            throw new MfcProjectServiceException("Found more than one project root entry!");
        }

        SVNDirEntry rootEntry = rootEntries.get(0);

        // Checkout root
        String projectRootPath = rootEntry.getRepositoryRoot().getPath();
        String projectName = projectRootPath.substring(projectRootPath.lastIndexOf("/") + 1);

        String projectWCPath = workingCopyDirectoryConfig.getRootDirectory() + "/" + projectName;

        try {
            svnService.checkout(
                    SvnTarget.fromFile(new File(projectWCPath)),
                    SvnTarget.fromURL(svnurl),
                    SVNDepth.EMPTY,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error checkout url [%s]: %s", remotePath, e.getMessage()));
        }

        // Search project descriptor
        List<String> projectExtensions = new ArrayList<>(Arrays.asList(".sln", "vcxproj"));
        List<SVNDirEntry> projectDescriptors = rootStructureList.stream()
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
        if(Objects.isNull(projectDescriptor)){

        }

        //Get project descriptor
        String projectDescriptorUrl = projectDescriptor.getURL().toString();
        String projectDescriptorRootUrl = projectDescriptor.getRepositoryRoot().toString();
        String wcPath = workingCopyDirectoryConfig.getRootDirectory() + projectDescriptorUrl.replace(projectDescriptorRootUrl, "");

        try {
            svnService.update(
                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromFile(new File(wcPath)))),
                    SVNDepth.FILES,
                    SVNRevision.HEAD,
                    true
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error error updating files: %s", e.getMessage()));
        }

        MfcProjectDTO resultDto = new MfcProjectDTO();
        resultDto.getSvnInfo().setUrl(rootEntry.getRepositoryRoot().toString());
        resultDto.getSvnInfo().setRevision(rootEntry.getRevision());



        return resultDto;



        // Search sources folder
//        List<SVNDirEntry> projectSourcesList = rootStructureList.stream()
//                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().compareToIgnoreCase("src") == 0)
//                .collect(Collectors.toList());
//
//        if(projectSourcesList.isEmpty()){
//            throw new MfcProjectServiceException("Missing project sources");
//        } else if(projectSourcesList.size() > 1){
//            throw new MfcProjectServiceException("Found more than one project sources!");
//        }
//
//        SVNDirEntry projectSources = projectSourcesList.get(0);
//
//        // Get content of source folder
//        List<SVNDirEntry> sourcesEntrie = null;
//        try {
//            sourcesEntrie = svnService.list(
//                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromURL(projectSources.getURL()))),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading sources folder for [%s]: %s", remotePath, e.getMessage()));
//        }
//
//        // Filter to get only resource files
//        List<SVNDirEntry> resources = sourcesEntrie
//                .stream()
//                .filter(svnDirEntry -> svnDirEntry.getName().endsWith(".rc"))
//                .collect(Collectors.toList());
//
//        if(Objects.isNull(resources)){
//
//        }
//
//        // Checkout root
//        String projectRootPath = rootEntry.getRepositoryRoot().getPath();
//        String projectName = projectRootPath.substring(projectRootPath.lastIndexOf("/") + 1);
//
//        String projectWCPath = workingCopyDirectoryConfig.getRootDirectory() + "/" + projectName;
//
//        try {
//            svnService.checkout(
//                    SvnTarget.fromFile(new File(projectWCPath)),
//                    SvnTarget.fromURL(svnurl),
//                    SVNDepth.EMPTY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error checkout url [%s]: %s", remotePath, e.getMessage()));
//        }
//
//        // Update only resouce files
//        List<SvnTarget> resourcesWC = resources
//                .stream()
//                .map(svnurl1 -> {
//                    String file = svnurl1.getURL().toString().replace(
//                            svnurl1.getRepositoryRoot().toString(),
//                            ""
//                    );
//                    return SvnTarget.fromFile(new File(projectWCPath + "\\" + file));
//                })
//                .collect(Collectors.toList());
//
//        try {
//            svnService.update(
//                    resourcesWC,
//                    SVNDepth.FILES,
//                    SVNRevision.HEAD,
//                    true
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error error updating files: %s", e.getMessage()));
//        }
//
//        MfcProjectDTO resultDto = new MfcProjectDTO();
//        resultDto.getSvnInfo().getRemote().setTarget(remotePath);
//        resultDto.getSvnInfo().getRemote().setName(projectName);
//        resultDto.getSvnInfo().getRemote().setRevision(rootEntry.getRevision());
//
//        resultDto.getSvnInfo().getLocal().setTarget(projectWCPath);
//        resultDto.getSvnInfo().getLocal().setName(projectName);
//        resultDto.getSvnInfo().getLocal().setRevision(rootEntry.getRevision());
//
//        return resultDto;
    }

    @Override
    public List<MfcProjectDTO> getProjects() throws MfcProjectServiceException {
        String workingCopyRootDirectory = workingCopyDirectoryConfig.getRootDirectory();
        File workingCopyRootDirectoryFile = new File(workingCopyRootDirectory);

        String[] wcProjects = workingCopyRootDirectoryFile.list((dir, name) -> new File(dir, name).isDirectory());

        List<MfcProjectDTO> mfcProjectDTOS = new ArrayList<>();
        for(var wcProject : wcProjects){

            String wcProjectPath = workingCopyDirectoryConfig.getRootDirectory() + "/" + wcProject;
            List<SvnStatus> statusListLocalList = null;
            try {
                statusListLocalList = svnService.status(
                        SvnTarget.fromFile(new File(wcProjectPath)),
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
                        SvnTarget.fromFile(new File(wcProjectPath)),
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


            mfcProjectDTO.setName(wcProject);

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


//    @Override
//    public MfcProjectDTO fetch(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException {
//        if(!mfcProjectDTO.getSvnInfo().getRemote().validateAllowedTarget()){
//            throw new MfcProjectServiceException(String.format("Cannot fetch new repository from file! Url: [%s]", mfcProjectDTO.getSvnInfo().getTarget()));
//        }
//
//        // Open provided URL
//        SVNURL svnurl = getSvnUrl(mfcProjectDTO.getSvnInfo().getTarget());
//        List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromURL(svnurl));
//
//        if(rootStructureUrl == null){
//            throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", svnurl.toString()));
//        }
//
//        //Project root entry
//        Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
//        if(projectRootEntry.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", mfcProjectDTO.getSvnInfo().getTarget()));
//        }
//
//        String projectName = getProjectName(projectRootEntry.get());
//        if(projectName.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Cannot find project name! Url: [%s]", mfcProjectDTO.getSvnInfo().getTarget()));
//        }
//
//        //Find project descriptor
//        Optional<SVNDirEntry> projectDescriptorEntry = findProjectDescriptorEntry(rootStructureUrl);
//        if(projectDescriptorEntry.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Repository [%s] has no descriptors!", svnurl.toString()));
//        }
//
//        //Found MFC project -> search for sources folder
//        Optional<SVNDirEntry> projectSourcesEntry = findProjectSourcesEntry(rootStructureUrl);
//        if(projectSourcesEntry.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Repository [{}] has no source folders!", svnurl.toString()));
//        }
//
//        List<SVNDirEntry> projectResourceEntries = findProjectResourceEntriesFromTarget(SvnTarget.fromURL(projectSourcesEntry.get().getURL()));
//        if(projectResourceEntries.size() == 0){
//            throw new MfcProjectServiceException(String.format("Repository [%s] has no resources!", svnurl.toString()));
//        }
//
//        //Checkout base project folder
//        String workingCopyPath = getWorkingCopyPath(projectRootEntry.get());
//        File workingCopyFile = new File(workingCopyPath);
//        checkoutProjectRootFolder(SvnTarget.fromFile(workingCopyFile), SvnTarget.fromURL(svnurl));
//
//        cleanupProjectWorkingCopy(SvnTarget.fromFile(workingCopyFile));
//
//        //Update only resource files
//        List<SvnTarget> workingCopyResourceTargets = projectResourceEntries
//                .stream()
//                .map(svnurl1 -> {
//                    String file = svnurl1.getURL().toString().replace(
//                            svnurl1.getRepositoryRoot().toString(),
//                            ""
//                    );
//                    return SvnTarget.fromFile(new File(workingCopyPath + "\\" + file));
//                })
//                .collect(Collectors.toList());
//        updateProjectWorkingCopy(workingCopyResourceTargets);
//
//        MfcProjectDTO resultDto = new MfcProjectDTO();
//        resultDto.getSvnInfo().setTarget(mfcProjectDTO.getSvnInfo().getTarget());
//        resultDto.getSvnInfo().setName(projectName);
//        resultDto.getSvnInfo().setRevision(projectRootEntry.get().getRevision());
//
//        return resultDto;
//    }
//
//    @Override
//    public MfcProjectDTO update(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException {
//        String workingCopyPath = workingCopyDirectoryConfig.getRootDirectory() + "\\" + mfcProjectDTO.getSvnInfo().getName();
//
//        // Open provided URL
//        File workingCopyRootDirectoryFile = new File(workingCopyPath);
//        List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromFile(workingCopyRootDirectoryFile));
//
//        if(rootStructureUrl == null){
//            throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", workingCopyPath));
//        }
//
//        Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
//        if(projectRootEntry.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", mfcProjectDTO.getSvnInfo().getTarget()));
//        }
//
//        Optional<SVNDirEntry> projectSourcesEntry = findProjectSourcesEntry(rootStructureUrl);
//        if(projectSourcesEntry.isEmpty()){
//            throw new MfcProjectServiceException(String.format("Repository [%s] has no source folders!", workingCopyPath));
//        }
//
//        List<SvnTarget> svnTargets = new ArrayList<>(Arrays.asList(SvnTarget.fromFile(workingCopyRootDirectoryFile)));
//        updateProjectWorkingCopy(svnTargets);
//
//        MfcProjectDTO resultDto = new MfcProjectDTO();
//        resultDto.getSvnInfo().setTarget(projectRootEntry.get().getRepositoryRoot().toString());
//        resultDto.getSvnInfo().setName(mfcProjectDTO.getSvnInfo().getName());
//        resultDto.getSvnInfo().setRevision(projectRootEntry.get().getRevision());
//
//        return resultDto;
//    }
//
//    @Override
//    public List<MfcProjectDTO> getWorkingCopies() throws MfcProjectServiceException {
//        String workingCopyRootDirectory = workingCopyDirectoryConfig.getRootDirectory();
//        File workingCopyRootDirectoryFile = new File(workingCopyRootDirectory);
//        String[] workingCopyProjectNamesArr = workingCopyRootDirectoryFile.list((dir, name) -> new File(dir, name).isDirectory());
//        List<String> workingCopyProjectNames = Arrays.stream(workingCopyProjectNamesArr)
//                .map(s -> workingCopyRootDirectory + "\\" + s)
//                .collect(Collectors.toList());
//
//        List<MfcProjectDTO> mfcProjectDTOS = new ArrayList<>();
//        for(var projectPath : workingCopyProjectNames){
//            File workingCopyDirectoryFile = new File(projectPath);
//            List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromFile(workingCopyDirectoryFile));
//
//            if(rootStructureUrl == null){
//                throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", projectPath));
//            }
//
//            Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
//            if(projectRootEntry.isEmpty()){
//                throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", projectPath));
//            }
//
//            String projectName = getProjectName(projectRootEntry.get());
//            if(projectName.isEmpty()){
//                throw new MfcProjectServiceException(String.format("Cannot find project name! Url: [%s]", projectPath));
//            }
//
//            MfcProjectDTO resultDto = new MfcProjectDTO();
//            resultDto.getSvnInfo().setTarget(projectPath);
//            resultDto.getSvnInfo().setName(projectName);
//            resultDto.getSvnInfo().setRevision(projectRootEntry.get().getRevision());
//            mfcProjectDTOS.add(resultDto);
//        }
//
//        return mfcProjectDTOS;
//    }
//
//    @Override
//    public List<MfcResourceDTO> getProjectResources(MfcProjectDTO mfcProjectDTO) throws MfcProjectServiceException, ResourcePropertiesServiceException {
//        String workingCopyPath = workingCopyDirectoryConfig.getRootDirectory() + "\\" + mfcProjectDTO.getSvnInfo().getName();
//        File workingCopyFile = new File(workingCopyPath);
//
//        List<SvnStatus> statusList = null;
//        try {
//            statusList = svnService.status(
//                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromFile(workingCopyFile))),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD,
//                    true,
//                    true);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
//        }
//
//        List<SvnStatus> resourcesStatus = statusList
//                .stream()
//                .filter(svnStatus -> svnStatus.getPath().getPath().endsWith(".rc"))
//                .collect(Collectors.toList());
//
//        List<SvnStatus> statusList1 = null;
//        try {
//            statusList1 = svnService.status(
//                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromFile(workingCopyFile))),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD,
//                    false,
//                    true);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
//        }
//
//        List<SvnStatus> resourcesStatus1 = statusList1
//                .stream()
//                .filter(svnStatus -> svnStatus.getPath().getPath().endsWith(".rc"))
//                .collect(Collectors.toList());
//
//        List<MfcResourceDTO> mfcResourceDTOS = new ArrayList<>();
//        for(var resourceStatus : resourcesStatus){
//            MfcResourceDTO mfcResourceDTO = new MfcResourceDTO();
//
//            File resourceFile = resourceStatus.getPath();
//            String resourceName = FilenameUtils.getName(resourceFile.getPath());
//
//            mfcResourceDTO.getSvnInfo().setName(resourceName);
//            mfcResourceDTO.getSvnInfo().setRevision(resourceStatus.getRevision());
//
//
//
//
//            String relativePath = relativize(workingCopyDirectoryConfig.getRootDirectory(), resourceStatus.getPath().getAbsolutePath());
//            mfcResourceDTO.getSvnInfo().setTarget(relativePath);
//
//            String[] split = relativePath.split("/");
//            String projectName = split[split.length - 2];
//
//            mfcResourceDTO.getMfcData().setFileName(resourceName);
//            mfcResourceDTO.getMfcData().setProjectName(projectName);
//
//            MfcResourceProperties mfcResourceProperties = mfcResourceParserService.parseResourceProperties(resourceFile);
//            mfcResourceDTO.setMfcResourceProperties(mfcResourceProperties);
//
//            mfcResourceDTOS.add(mfcResourceDTO);
//
//        }
//        return mfcResourceDTOS;
//    }
//
//    private SVNURL getSvnUrl(String url) throws MfcProjectServiceException {
//        try {
//            return SVNURL.parseURIEncoded(url);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Cannot parse URL: %s", url));
//        }
//    }
//
//
//    private List<SVNDirEntry> getProjectRootStructure(SvnTarget target) throws MfcProjectServiceException {
//        List<SVNDirEntry> remoteRepositoryList = null;
//        try {
//            remoteRepositoryList = svnService.list(
//                    new ArrayList<SvnTarget>(Arrays.asList(target)),
//                    SVNDepth.IMMEDIATES,
//                    SVNRevision.HEAD);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading root project structure for [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
//        }
//
//        return remoteRepositoryList;
//    }
//
//    private Optional<SVNDirEntry> findProjectDescriptorEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
//        List<String> projectExtensions = new ArrayList<>(Arrays.asList(".sln", "vcxproj"));
//        List<SVNDirEntry> founds = entries.stream()
//                .filter(svnTargetSVNDirEntryEntry -> {
//                    return projectExtensions.stream().anyMatch(s -> svnTargetSVNDirEntryEntry.getName().endsWith(s));
//                })
//                .collect(Collectors.toList());
//
//        if(founds.size() > 1){
//            throw new MfcProjectServiceException("Found more than one project descriptor!");
//        }
//
//        if(!founds.isEmpty()){
//            return Optional.of(founds.get(0));
//        } else {
//            return Optional.empty();
//        }
//    }
//
//    private Optional<SVNDirEntry> findProjectRootEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
//        List<SVNDirEntry> founds = entries.stream()
//                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().isEmpty())
//                .collect(Collectors.toList());
//
//        if(founds.size() > 1){
//            throw new MfcProjectServiceException("Found more than one project root directories!");
//        }
//
//        if(!founds.isEmpty()){
//            return Optional.of(founds.get(0));
//        } else {
//            return Optional.empty();
//        }
//    }
//
//    private String getProjectName(SVNDirEntry entry){
//        String projectPath = entry.getRepositoryRoot().getPath();
//        return projectPath.substring(projectPath.lastIndexOf("/") + 1);
//    }
//
//    private Optional<SVNDirEntry> findProjectSourcesEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
//        List<SVNDirEntry> founds = entries.stream()
//                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().compareToIgnoreCase("src") == 0)
//                .collect(Collectors.toList());
//
//        if(founds.size() > 1){
//            throw new MfcProjectServiceException("Found more than one project source directories!");
//        }
//
//        if(!founds.isEmpty()){
//            return Optional.of(founds.get(0));
//        } else {
//            return Optional.empty();
//        }
//    }
//
//    private List<SVNDirEntry> filterResourcesFromSvnEntry(List<SVNDirEntry> resources){
//        return resources.stream()
//                .filter(svnDirEntry -> svnDirEntry.getName().endsWith(".rc"))
//                .collect(Collectors.toList());
//    }
//
//    private List<SvnInfo> filterResourcesFromSvnInfo(List<SvnInfo> resources){
//        return resources.stream()
//                .filter(svnDirEntry -> svnDirEntry.getUrl().toString().endsWith(".rc"))
//                .collect(Collectors.toList());
//    }
//
//    private List<SVNDirEntry> findProjectResourceEntriesFromTarget(SvnTarget target) throws MfcProjectServiceException {
//        List<SVNDirEntry> resources = null;
//        try {
//            resources = svnService.list(
//                    new ArrayList<SvnTarget>(Arrays.asList(target)),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading sources folder for [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
//        }
//
//        return filterResourcesFromSvnEntry(resources);
//    }
//
//    private List<SVNDirEntry> findProjectResourceEntriesFromTargetsOnly(List<SvnTarget> targets) throws MfcProjectServiceException {
//        List<SVNDirEntry> resources = null;
//        try {
//            resources = svnService.list(
//                    targets,
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading targets: [%s]", e.getMessage()));
//        }
//
//        return filterResourcesFromSvnEntry(resources);
//    }
//
//    private List<String> findProjectResourcesInWorkingCopy(String rootPath) throws IOException {
//        return Files.find(Paths.get(rootPath), Integer.MAX_VALUE, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile())
//                .map(path -> path.toString())
//                .collect(Collectors.toList());
//    }
//
//    private List<SvnInfo> findProjectResourceEntriesFromSvnInfo(SvnTarget target) throws MfcProjectServiceException {
//        List<SvnInfo> resources = null;
//        try {
//            resources = svnService.info(
//                    new ArrayList<SvnTarget>(Arrays.asList(target)),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading sources folder for [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
//        }
//
//        return filterResourcesFromSvnInfo(resources);
//    }
//
//    private List<SvnInfo> findProjectResourceEntriesFromSvnInfo1(List<SvnTarget> target) throws MfcProjectServiceException {
//        List<SvnInfo> resources = null;
//        try {
//            resources = svnService.info(
//                    target,
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD);
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error loading sources folder for [%s]", e.getMessage()));
//        }
//
//        return filterResourcesFromSvnInfo(resources);
//    }
//
//    private String getWorkingCopyPath(SVNDirEntry entry){
//        return workingCopyDirectoryConfig.getRootDirectory() + "/" + getProjectName(entry);
//    }
//
//    private void checkoutProjectRootFolder(SvnTarget target, SvnTarget source) throws MfcProjectServiceException {
//        try {
//            svnService.checkout(
//                    target,
//                    source,
//                    SVNDepth.EMPTY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error checkout url [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
//        }
//    }
//
//    private void cleanupProjectWorkingCopy(SvnTarget target) throws MfcProjectServiceException {
//        try {
//            svnService.cleanup(
//                    new ArrayList<SvnTarget>(Arrays.asList(target)),
//                    SVNDepth.INFINITY,
//                    SVNRevision.HEAD
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error error cleanup file [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
//        }
//    }
//
//    private void updateProjectWorkingCopy(List<SvnTarget> targets) throws MfcProjectServiceException {
//        try {
//            svnService.update(
//                    targets,
//                    SVNDepth.FILES,
//                    SVNRevision.HEAD,
//                    true
//            );
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(String.format("Error error updating files: %s", e.getMessage()));
//        }
//    }
//
//    private String getNameFromSvnInfo(SvnInfo svnInfo){
//        String[] strArray = svnInfo.getUrl().toString().split("/");
//        return strArray[strArray.length -1];
//    }
//
//    public String relativeToRoot(SVNDirEntry svnDirEntry){
//        String repositoryRoot = svnDirEntry.getRepositoryRoot().toString();
//        String currentDir = svnDirEntry.getURL().toString();
//        return currentDir.replace(repositoryRoot, "");
//    }
//
//    public String relativeToRoot(SvnInfo svnInfo){
//        String repositoryRoot = svnInfo.getRepositoryRootUrl().toString();
//        String currentDir = svnInfo.getUrl().toString();
//        return currentDir.replace(repositoryRoot, "");
//    }
//
//    private String relativize(String base, String path){
//        return new File(base).toURI().relativize(new File(path).toURI()).getPath();
//    }
}
