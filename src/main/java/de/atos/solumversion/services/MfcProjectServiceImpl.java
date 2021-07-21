package de.atos.solumversion.services;

import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import de.atos.solumversion.dto.ProjectDTO;
import de.atos.solumversion.dto.ResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.*;

import java.io.File;
import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@SessionScope
public class MfcProjectServiceImpl implements MfcProjectService {

    private SvnService svnService;
    private WorkingCopyDirectoryConfig workingCopyDirectoryConfig;

    public MfcProjectServiceImpl(SvnService svnService, WorkingCopyDirectoryConfig workingCopyDirectoryConfig) {
        this.svnService = svnService;
        this.workingCopyDirectoryConfig = workingCopyDirectoryConfig;
    }

    @Override
    public ProjectDTO fetchProject(ProjectDTO projectDTO) throws MfcProjectServiceException {
        if(!projectDTO.getSvnData().validateAllowedTarget()){
            throw new MfcProjectServiceException(String.format("Cannot fetch new repository from file! Url: [%s]", projectDTO.getSvnData().getTarget()));
        }

        // Open provided URL
        SVNURL svnurl = getSvnUrl(projectDTO.getSvnData().getTarget());
        List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromURL(svnurl));

        if(rootStructureUrl == null){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", svnurl.toString()));
        }

        //Project root entry
        Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
        if(projectRootEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", projectDTO.getSvnData().getTarget()));
        }

        String projectName = getProjectName(projectRootEntry.get());
        if(projectName.isEmpty()){
            throw new MfcProjectServiceException(String.format("Cannot find project name! Url: [%s]", projectDTO.getSvnData().getTarget()));
        }

        //Find project descriptor
        Optional<SVNDirEntry> projectDescriptorEntry = findProjectDescriptorEntry(rootStructureUrl);
        if(projectDescriptorEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no descriptors!", svnurl.toString()));
        }

        //Found MFC project -> search for sources folder
        Optional<SVNDirEntry> projectSourcesEntry = findProjectSourcesEntry(rootStructureUrl);
        if(projectSourcesEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Repository [{}] has no source folders!", svnurl.toString()));
        }

        List<SVNDirEntry> projectResourceEntries = findProjectResourceEntries(SvnTarget.fromURL(projectSourcesEntry.get().getURL()));
        if(projectResourceEntries.size() == 0){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no resources!", svnurl.toString()));
        }

        //Checkout base project folder
        String workingCopyPath = getWorkingCopyPath(projectRootEntry.get());
        File workingCopyFile = new File(workingCopyPath);
        checkoutProjectRootFolder(SvnTarget.fromFile(workingCopyFile), SvnTarget.fromURL(svnurl));

        cleanupProjectWorkingCopy(SvnTarget.fromFile(workingCopyFile));

        //Update only resource files
        List<SvnTarget> workingCopyResourceTargets = projectResourceEntries
                .stream()
                .map(svnurl1 -> {
                    String file = svnurl1.getURL().toString().replace(
                            svnurl1.getRepositoryRoot().toString(),
                            ""
                    );
                    return SvnTarget.fromFile(new File(workingCopyPath + "\\" + file));
                })
                .collect(Collectors.toList());
        updateProjectWorkingCopy(workingCopyResourceTargets);

        ProjectDTO resultDto = new ProjectDTO();
        resultDto.getSvnData().setTarget(projectDTO.getSvnData().getTarget());
        resultDto.getSvnData().setName(projectName);
        resultDto.getSvnData().setRevision(projectRootEntry.get().getRevision());

        return resultDto;
    }

    @Override
    public ProjectDTO updateWholeWorkingCopyProject(ProjectDTO projectDTO) throws MfcProjectServiceException {
        String workingCopyPath = workingCopyDirectoryConfig.getRootDirectory() + "\\" + projectDTO.getSvnData().getName();

        // Open provided URL
        File workingCopyRootDirectoryFile = new File(workingCopyPath);
        List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromFile(workingCopyRootDirectoryFile));

        if(rootStructureUrl == null){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", workingCopyPath));
        }

        Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
        if(projectRootEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", projectDTO.getSvnData().getTarget()));
        }

        Optional<SVNDirEntry> projectSourcesEntry = findProjectSourcesEntry(rootStructureUrl);
        if(projectSourcesEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no source folders!", workingCopyPath));
        }

        List<SvnTarget> svnTargets = new ArrayList<>(Arrays.asList(SvnTarget.fromFile(workingCopyRootDirectoryFile)));
        updateProjectWorkingCopy(svnTargets);

        ProjectDTO resultDto = new ProjectDTO();
        resultDto.getSvnData().setTarget(projectRootEntry.get().getRepositoryRoot().toString());
        resultDto.getSvnData().setName(projectDTO.getSvnData().getName());
        resultDto.getSvnData().setRevision(projectRootEntry.get().getRevision());

        return resultDto;
    }

    @Override
    public List<ProjectDTO> getWorkingCopyProjects() throws MfcProjectServiceException {
        String workingCopyRootDirectory = workingCopyDirectoryConfig.getRootDirectory();
        File workingCopyRootDirectoryFile = new File(workingCopyRootDirectory);
        String[] workingCopyProjectNamesArr = workingCopyRootDirectoryFile.list((dir, name) -> new File(dir, name).isDirectory());
        List<String> workingCopyProjectNames = Arrays.stream(workingCopyProjectNamesArr)
                .map(s -> workingCopyRootDirectory + "\\" + s)
                .collect(Collectors.toList());

        List<ProjectDTO> projectDTOS = new ArrayList<>();
        for(var projectPath : workingCopyProjectNames){
            File workingCopyDirectoryFile = new File(projectPath);
            List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromFile(workingCopyDirectoryFile));

            if(rootStructureUrl == null){
                throw new MfcProjectServiceException(String.format("Repository [%s] has no items!", projectPath));
            }

            Optional<SVNDirEntry> projectRootEntry = findProjectRootEntry(rootStructureUrl);
            if(projectRootEntry.isEmpty()){
                throw new MfcProjectServiceException(String.format("Cannot find root directory!. Url: [%s]", projectPath));
            }

            String projectName = getProjectName(projectRootEntry.get());
            if(projectName.isEmpty()){
                throw new MfcProjectServiceException(String.format("Cannot find project name! Url: [%s]", projectPath));
            }

            ProjectDTO resultDto = new ProjectDTO();
            resultDto.getSvnData().setTarget(projectPath);
            resultDto.getSvnData().setName(projectName);
            resultDto.getSvnData().setRevision(projectRootEntry.get().getRevision());
            projectDTOS.add(resultDto);
        }

        return projectDTOS;
    }

    @Override
    public List<ResourceDTO> getProjectResources(ProjectDTO projectDTO) throws MfcProjectServiceException {
        String workingCopyPath = workingCopyDirectoryConfig.getRootDirectory() + "\\" + projectDTO.getSvnData().getName();

        // Open provided URL
        File workingCopyRootDirectoryFile = new File(workingCopyPath);
        List<SVNDirEntry> rootStructureUrl = getProjectRootStructure(SvnTarget.fromFile(workingCopyRootDirectoryFile));

        Optional<SVNDirEntry> projectSourcesEntry = findProjectSourcesEntry(rootStructureUrl);
        if(projectSourcesEntry.isEmpty()){
            throw new MfcProjectServiceException(String.format("Repository [{}] has no source folders!", workingCopyPath));
        }

        List<SVNDirEntry> projectResourceEntries = findProjectResourceEntries(SvnTarget.fromURL(projectSourcesEntry.get().getURL()));
        if(projectResourceEntries.size() == 0){
            throw new MfcProjectServiceException(String.format("Repository [%s] has no resources!", workingCopyPath));
        }

        List<ResourceDTO> resourceDTOS = new ArrayList<>();
        for(var resource : projectResourceEntries){
            ResourceDTO resourceDTO = new ResourceDTO();
            resourceDTO.getSvnData().setName(resource.getName());
            resourceDTO.getSvnData().setTarget(resource.getURL().toString());
            resourceDTO.getSvnData().setRevision(resource.getRevision());


        }

        return null;
    }

    private SVNURL getSvnUrl(String url) throws MfcProjectServiceException {
        try {
            return SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Cannot parse URL: %s", url));
        }
    }


    private List<SVNDirEntry> getProjectRootStructure(SvnTarget target) throws MfcProjectServiceException {
        List<SVNDirEntry> remoteRepositoryList = null;
        try {
            remoteRepositoryList = svnService.list(
                    new ArrayList<SvnTarget>(Arrays.asList(target)),
                    SVNDepth.IMMEDIATES,
                    SVNRevision.HEAD);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error loading root project structure for [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
        }

        return remoteRepositoryList;
    }

    private Optional<SVNDirEntry> findProjectDescriptorEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
        List<String> projectExtensions = new ArrayList<>(Arrays.asList(".sln", "vcxproj"));
        List<SVNDirEntry> founds = entries.stream()
                .filter(svnTargetSVNDirEntryEntry -> {
                    return projectExtensions.stream().anyMatch(s -> svnTargetSVNDirEntryEntry.getName().endsWith(s));
                })
                .collect(Collectors.toList());

        if(founds.size() > 1){
            throw new MfcProjectServiceException("Found more than one project descriptor!");
        }

        if(!founds.isEmpty()){
            return Optional.of(founds.get(0));
        } else {
            return Optional.empty();
        }
    }

    private Optional<SVNDirEntry> findProjectRootEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
        List<SVNDirEntry> founds = entries.stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().isEmpty())
                .collect(Collectors.toList());

        if(founds.size() > 1){
            throw new MfcProjectServiceException("Found more than one project root directories!");
        }

        if(!founds.isEmpty()){
            return Optional.of(founds.get(0));
        } else {
            return Optional.empty();
        }
    }

    private String getProjectName(SVNDirEntry entry){
        String projectPath = entry.getRepositoryRoot().getPath();
        return projectPath.substring(projectPath.lastIndexOf("/") + 1);
    }

    private Optional<SVNDirEntry> findProjectSourcesEntry(List<SVNDirEntry> entries) throws MfcProjectServiceException {
        List<SVNDirEntry> founds = entries.stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getName().compareToIgnoreCase("src") == 0)
                .collect(Collectors.toList());

        if(founds.size() > 1){
            throw new MfcProjectServiceException("Found more than one project source directories!");
        }

        if(!founds.isEmpty()){
            return Optional.of(founds.get(0));
        } else {
            return Optional.empty();
        }
    }

    private List<SVNDirEntry> findProjectResourceEntries(SvnTarget target) throws MfcProjectServiceException {
        List<SVNDirEntry> resources = null;
        try {
            resources = svnService.list(
                    new ArrayList<SvnTarget>(Arrays.asList(target)),
                    SVNDepth.INFINITY,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error loading sources folder for [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
        }

        List<SVNDirEntry> desiredResources = resources.stream()
                .filter(svnDirEntry -> svnDirEntry.getName().endsWith(".rc"))
                .collect(Collectors.toList());

        return desiredResources;
    }

    private String getWorkingCopyPath(SVNDirEntry entry){
        return workingCopyDirectoryConfig.getRootDirectory() + "/" + getProjectName(entry);
    }

    private void checkoutProjectRootFolder(SvnTarget target, SvnTarget source) throws MfcProjectServiceException {
        try {
            svnService.checkout(
                    target,
                    source,
                    SVNDepth.EMPTY,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error checkout url [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
        }
    }

    private void cleanupProjectWorkingCopy(SvnTarget target) throws MfcProjectServiceException {
        try {
            svnService.cleanup(
                    new ArrayList<SvnTarget>(Arrays.asList(target)),
                    SVNDepth.INFINITY,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error error cleanup file [%s]: %s", target.getPathOrUrlString(), e.getMessage()));
        }
    }

    private void updateProjectWorkingCopy(List<SvnTarget> targets) throws MfcProjectServiceException {
        try {
            svnService.update(
                    targets,
                    SVNDepth.FILES,
                    SVNRevision.HEAD,
                    true
            );
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Error error updating files: %s", e.getMessage()));
        }
    }
}
