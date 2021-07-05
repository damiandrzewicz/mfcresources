package de.atos.solumversion.services;

import de.atos.solumversion.domain.MfcSubversionProject;
import de.atos.solumversion.domain.SubversionDirectory;
import de.atos.solumversion.domain.SubversionFile;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MfcSubversionService {

    String workingCopySvnRoot = "svn";

    private SubversionService subversionService;

    public MfcSubversionService(SubversionService subversionService) {
        this.subversionService = subversionService;
    }

    public void init(String url) throws MfcSubversionServiceException {
        try {
            SVNURL svnurl = SVNURL.parseURIEncoded(url);
            subversionService.open(svnurl);
        } catch (SVNAuthenticationException e){
            throw new MfcSubversionServiceException("authentication error");
        } catch (SVNException e) {
            throw new MfcSubversionServiceException("malformed url");
        }
    }

    public void authentiate(String user, String password) {
        subversionService.authenticate(user, password);
    }

    public MfcSubversionProject loadProject() throws MfcSubversionServiceException {
        // Check project
        Optional<SubversionDirectory> project = null;
        try {
            project = checkProject();
            if(project.isEmpty()){
                throw new MfcSubversionServiceException("not desired project(.sln, .dsw)");
            }
        } catch (SVNException e) {
            throw new MfcSubversionServiceException("checking project error");
        }

        SubversionDirectory subversionDirectory = project.get();

        // Get project resources
        //Find all resource files
        List<SubversionFile> projectResources;
        try {
            projectResources = findProjectResources();
        } catch (SVNException e) {
            throw new MfcSubversionServiceException(String.format("cannot load resources for project: [%s]", subversionDirectory.getUrl().toString()));
        }

        // Check if working copy exists
        Optional<SVNInfo> workingCopy = isWorkingCopyExists(subversionDirectory.getName());
        if(workingCopy.isEmpty()){
            // Create working copy
            // Checkout root folder
            SVNURL svnUrl;
            try {
                svnUrl = SVNURL.parseURIEncoded(subversionDirectory.getRepositoryRoot());
            } catch (SVNException e) {
                throw new MfcSubversionServiceException("malformed svn root directory");
            }
            File wcDir = new File(workingCopySvnRoot + "/" + subversionDirectory.getName());
            try {
                subversionService.checkoutFolder(svnUrl, SVNRevision.HEAD, wcDir, SVNDepth.EMPTY);
            } catch (SVNException e) {
                throw new MfcSubversionServiceException(String.format("error checking out project: [%s]", svnUrl.toString()));
            }

            // Load resources into array
            List files = new ArrayList();
            for(var resource : projectResources){
                files.add(resource.getUrl());
            }

            //Update resources with parents
            try {
                subversionService.updateFiles((File[])files.toArray(), SVNRevision.HEAD, SVNDepth.FILES);
            } catch (SVNException e) {
                throw new MfcSubversionServiceException("error updating files");
            }
            // Ok, return what necessary
        } else {
            // Working copy exists
            // Only update files
            SVNInfo svnInfo = workingCopy.get();

            //Update resources with parents
            File workingDir = new File(svnInfo.getPath());
            try {
                subversionService.updateFiles(new File[]{workingDir}, SVNRevision.HEAD, SVNDepth.FILES);
            } catch (SVNException e) {
                throw new MfcSubversionServiceException("error updating files");
            }
            // Ok, return what necessary
        }

        MfcSubversionProject mfcSubversionProject = new MfcSubversionProject();
        mfcSubversionProject.setProjectDirectory(subversionDirectory);
        mfcSubversionProject.setResources(projectResources);

        return mfcSubversionProject;
    }

    public void commit(File[] files, String commitMessage) throws MfcSubversionServiceException {

        try {
            SVNCommitInfo commit = subversionService.commit(files, false, commitMessage);
        } catch (SVNException e) {
            throw new MfcSubversionServiceException("error commit choosen files");
        }
    }


    private Optional<SubversionDirectory> checkProject() throws SVNException {
        Collection entries = subversionService.getDirEntries("", SVNRevision.HEAD);
        Iterator iterator = entries.iterator();
        String[] extensions = {"sln", "dsw"};
        while(iterator.hasNext()){
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            if(checkIfFileHasExtension(entry.getName(), extensions)){
                SubversionDirectory directory = new SubversionDirectory();
                directory.setUrl(entry.getURL().toString());
                String[] split = entry.getRepositoryRoot().toString().split("/");
                String name = split[split.length-1];
                directory.setName(name);
                return Optional.of(directory);
            }
        }
        return Optional.empty();
    }


    private Optional<SVNInfo> isWorkingCopyExists(String name){
        File file = new File(workingCopySvnRoot + "/" + name);
        try {
            SVNInfo svnInfo = subversionService.wcInfo(file, SVNRevision.HEAD);
            return Optional.of(svnInfo);
        } catch (SVNException e) {
            return Optional.empty();
        }
    }

    public List<SubversionFile> findProjectResources() throws SVNException {
        List resources = new ArrayList();
        String sourcesPath = "Src";
        Collection entries = subversionService.getDirEntries(sourcesPath, SVNRevision.HEAD);
        Iterator iterator = entries.iterator();
        while(iterator.hasNext()){
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            if(entry.getKind() == SVNNodeKind.DIR){
                Collection projectEntries = subversionService.getDirEntries(sourcesPath + "/" + entry.getName(), SVNRevision.HEAD);
                Iterator projectIterator = projectEntries.iterator();
                String[] resourceExtension = { "rc" };
                while(projectIterator.hasNext()){
                    SVNDirEntry projectEntry = (SVNDirEntry) projectIterator.next();
                    if(projectEntry.getKind() == SVNNodeKind.FILE && checkIfFileHasExtension(projectEntry.getName(), resourceExtension)){
                        SubversionFile subversionFile = new SubversionFile();
                        subversionFile.setNameWithExtension(projectEntry.getName());
                        subversionFile.setUrl(projectEntry.getURL().toString());
                        subversionFile.setRepositoryRoot(projectEntry.getRepositoryRoot().toString());
                        subversionFile.setCreated(projectEntry.getDate());
                        resources.add(subversionFile);
                    }
                }
            }
        }
        return resources;
    }

    private static boolean checkIfFileHasExtension(String s, String[] extn){
        return Arrays.stream(extn).anyMatch(s::endsWith);
    }
}
