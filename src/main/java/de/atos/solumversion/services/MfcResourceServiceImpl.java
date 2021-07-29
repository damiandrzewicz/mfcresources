package de.atos.solumversion.services;

import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.CommitInfoDTO;
import de.atos.solumversion.dto.MfcProjectDTO;
import de.atos.solumversion.dto.MfcResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc2.SvnStatus;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MfcResourceServiceImpl implements MfcResourceService{

    private SvnService svnService;

    private WorkingCopyDirectoryConfig workingCopyDirectoryConfig;

    private MfcResourceParserService mfcResourceParserService;

    public MfcResourceServiceImpl(SvnService svnService, WorkingCopyDirectoryConfig workingCopyDirectoryConfig, MfcResourceParserService mfcResourceParserService) {
        this.svnService = svnService;
        this.workingCopyDirectoryConfig = workingCopyDirectoryConfig;
        this.mfcResourceParserService = mfcResourceParserService;
    }

    @Override
    public List<MfcResourceDTO> getResources(MfcProjectDTO mfcProjectDTO) throws MfcResourceServiceException {
        
        Path localSourcesPath = Paths.get(workingCopyDirectoryConfig.getRootDirectory(), mfcProjectDTO.getName(), "Src");

        // Get info about working copy
        List<SvnStatus> filesInSrcLocal = null;
        try {
            filesInSrcLocal = svnService.status(
                    SvnTarget.fromFile(new File(localSourcesPath.toString())),
                    SVNDepth.INFINITY,
                    SVNRevision.HEAD,
                    false,
                    true);
        } catch (SVNException e) {
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
        }

        if(Objects.isNull(filesInSrcLocal)){
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "sources folder is empty"));
        }

        List<SvnStatus> localResources = filesInSrcLocal
                .stream()
                .filter(svnStatus -> svnStatus.getPath().toString().endsWith(".rc"))
                .collect(Collectors.toList());

        if(Objects.isNull(localResources)){
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "resources not found"));
        }

        // Get info from remote
        List<SvnStatus> filesInSrcRemote = null;
        try {
            filesInSrcRemote = svnService.status(
                    SvnTarget.fromFile(new File(localSourcesPath.toString())),
                    SVNDepth.INFINITY,
                    SVNRevision.HEAD,
                    true,
                    true);
        } catch (SVNException e) {
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
        }

        if(Objects.isNull(filesInSrcRemote)){
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "sources folder is empty"));
        }

        List<SvnStatus> remoteResources = filesInSrcRemote
                .stream()
                .filter(svnStatus -> svnStatus.getPath().toString().endsWith(".rc"))
                .collect(Collectors.toList());

        if(Objects.isNull(localResources)){
            throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "resources not found"));
        }

        // Get resources info

        List<MfcResourceDTO> result = new ArrayList<>();
        for(var resource : localResources){
            MfcResourceDTO mfcResourceDTO  =new MfcResourceDTO();

            Path path = Paths.get(resource.getPath().toString());

            mfcResourceDTO.setResourceNameWithExtension(path.getFileName().toString());

            mfcResourceDTO.setProjectName(path.getParent().getFileName().toString());

            mfcResourceDTO.getSvnInfo().setRevision(resource.getRevision());

            mfcResourceDTO.getSvnInfo().setLastCommitAuthor(resource.getChangedAuthor());


            String relativePath = Paths.get(workingCopyDirectoryConfig.getRootDirectory()).toUri().relativize(resource.getPath().toURI()).getPath();
            mfcResourceDTO.getSvnInfo().setUrl(relativePath);

            Optional<SvnStatus> remoteResource = remoteResources
                    .stream()
                    .filter(svnStatus -> svnStatus.getRepositoryRelativePath().compareTo(resource.getRepositoryRelativePath().toString()) == 0)
                    .findFirst();
            mfcResourceDTO.getSvnInfo().setOutdated(resource.getRevision() < remoteResource.get().getRevision());


            mfcResourceDTO.getSvnInfo().setHasLocalModifications(resource.getTextStatus() != SVNStatusType.STATUS_NORMAL);

            result.add(mfcResourceDTO);
        }

        return result;
    }

    @Override
    public List<MfcResourceDTO> updateResources(List<MfcResourceDTO> mfcResourceDTO) {
        return null;
    }

    @Override
    public CommitInfoDTO commit(CommitDTO commitDTO) {
        String message = commitDTO.getMessage();

        List<SvnTarget> targets = commitDTO.getUrls()
                .stream()
                .map(url -> SvnTarget.fromFile(Paths.get(workingCopyDirectoryConfig.getRootDirectory(), url).toFile()))
                .collect(Collectors.toList());

        SVNCommitInfo commitInfo = null;
        try {
            commitInfo = svnService.commit(
                    targets,
                    SVNDepth.EMPTY,
                    SVNRevision.HEAD,
                    message
            );
        } catch (SVNException e) {
            e.printStackTrace();
        }

        CommitInfoDTO commitInfoDTO = new CommitInfoDTO();
        commitInfoDTO.setNewRevision(commitInfo.getNewRevision());
        commitInfoDTO.setCommitAuthor(commitInfo.getAuthor());

        return commitInfoDTO;
    }

    @Override
    public List<MfcResourceDTO> update(List<MfcResourceDTO> mfcResourceDTOS) throws MfcResourceServiceException {
        String rootDirectory = workingCopyDirectoryConfig.getRootDirectory();

        List<SvnTarget> resourcePaths = mfcResourceDTOS
                .stream()
                .map(mfcResourceDTO1 -> SvnTarget.fromFile(Paths.get(rootDirectory, mfcResourceDTO1.getSvnInfo().getUrl()).toFile()))
                .collect(Collectors.toList());

        try {
            svnService.update(
                    resourcePaths,
                    SVNDepth.EMPTY,
                    SVNRevision.HEAD,
                    false
            );
        } catch (SVNException e) {
            e.printStackTrace();
        }

        // Get info about working copy

        List<MfcResourceDTO> result = new ArrayList<>();
        for(var resourceDTO : mfcResourceDTOS){
            File file = Paths.get(rootDirectory, resourceDTO.getSvnInfo().getUrl()).toFile();

            List<SvnStatus> filesInSrcLocal = null;
            try {
                filesInSrcLocal = svnService.status(
                        SvnTarget.fromFile(file),
                        SVNDepth.INFINITY,
                        SVNRevision.HEAD,
                        false,
                        true);
            } catch (SVNException e) {
                throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
            }

            if(Objects.isNull(filesInSrcLocal)){
                throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "sources folder is empty"));
            }

            SvnStatus localStatus = filesInSrcLocal.get(0);

            // Get info from remote
            List<SvnStatus> filesInSrcRemote = null;
            try {
                filesInSrcRemote = svnService.status(
                        SvnTarget.fromFile(file),
                        SVNDepth.INFINITY,
                        SVNRevision.HEAD,
                        true,
                        true);
            } catch (SVNException e) {
                throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", e.getMessage()));
            }

            if(Objects.isNull(filesInSrcRemote)){
                throw new MfcResourceServiceException(String.format("Error getting resources: [%s]", "sources folder is empty"));
            }

            SvnStatus remoteStatus = filesInSrcRemote.get(0);

            MfcResourceDTO mfcResourceDTO  =new MfcResourceDTO();

            Path path = Paths.get(localStatus.getPath().toString());

            mfcResourceDTO.setResourceNameWithExtension(path.getFileName().toString());

            mfcResourceDTO.setProjectName(path.getParent().getFileName().toString());

            mfcResourceDTO.getSvnInfo().setRevision(localStatus.getRevision());


            String relativePath = Paths.get(workingCopyDirectoryConfig.getRootDirectory()).toUri().relativize(localStatus.getPath().toURI()).getPath();
            mfcResourceDTO.getSvnInfo().setUrl(relativePath);


            mfcResourceDTO.getSvnInfo().setOutdated(localStatus.getRevision() < remoteStatus.getRevision());


            mfcResourceDTO.getSvnInfo().setHasLocalModifications(localStatus.getTextStatus() != SVNStatusType.STATUS_NORMAL);

            result.add(mfcResourceDTO);
        }

        return result;
    }

    @Override
    public MfcResourceDTO revert(MfcResourceDTO mfcResourceDTO) {
        return null;
    }
}
