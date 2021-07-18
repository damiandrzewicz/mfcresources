package de.atos.solumversion.services;

import de.atos.solumversion.configuration.WorkingCopyDirectoryConfig;
import de.atos.solumversion.dto.CommitDTO;
import de.atos.solumversion.dto.SvnItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.*;

import java.io.File;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    public SvnItemDTO fetch(SvnItemDTO svnItemDTO) throws MfcProjectServiceException {
        SvnItemDTO resultDto = new SvnItemDTO();

        // Open provided URL
        SVNURL svnurl = getSvnUrl(svnItemDTO.getRemoteData().getUrl());

        Map<SvnTarget, SVNDirEntry> remoteRepositoryList = null;
        try {
            remoteRepositoryList = svnService.list(
                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromURL(svnurl))),
                    SVNDepth.IMMEDIATES,
                    SVNRevision.HEAD);
        } catch (SVNException e) {
            e.printStackTrace();
        }

        if(remoteRepositoryList == null){
            throw new MfcProjectServiceException(String.format("Repository [{}] has no items!", svnurl.toString()));
        }

        //Find project entry
        Optional<Map.Entry<SvnTarget, SVNDirEntry>> rootDirectoryEntry = remoteRepositoryList.entrySet().stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getValue().getRepositoryRoot() == svnTargetSVNDirEntryEntry.getValue().getURL())
                .reduce((svnTargetSVNDirEntryEntry, svnTargetSVNDirEntryEntry2) -> {
                    throw new IllegalStateException(String.format("Cannot find root directory for repository [{}]", svnurl.toString()));
                });

        //Check if has project file ald sources folder
        List<String> projectExtensions = new ArrayList<>(Arrays.asList(".sln", "vcxproj"));
        Optional<Map.Entry<SvnTarget, SVNDirEntry>> projectFile = remoteRepositoryList.entrySet().stream()
                .filter(svnTargetSVNDirEntryEntry -> {
                    return projectExtensions.stream().anyMatch(s -> svnTargetSVNDirEntryEntry.getValue().getName().endsWith(s));
                })
                .reduce((svnTargetSVNDirEntryEntry, svnTargetSVNDirEntryEntry2) -> {
                    throw new IllegalStateException(String.format("Found more than one project file for repository [{}]", svnurl.toString()));
                });

        if(projectFile.isEmpty()){

        }

        //Found MFC project -> search for sources folder
        Optional<Map.Entry<SvnTarget, SVNDirEntry>> srcFolder = remoteRepositoryList.entrySet().stream()
                .filter(svnTargetSVNDirEntryEntry -> svnTargetSVNDirEntryEntry.getValue().getName().compareToIgnoreCase("src") == 0)
                .reduce((svnTargetSVNDirEntryEntry, svnTargetSVNDirEntryEntry2) -> {
                    throw new IllegalStateException(String.format("Found more than one sources folder for repository [{}]", svnurl.toString()));
                });
        //.collect(Collectors.toList());

        if(srcFolder.isEmpty()){

        }

        //Found sources folder -> search for all resources
        SVNURL srcUrl = srcFolder.get().getValue().getURL();
        Map<SvnTarget, SVNDirEntry> resources = null;
        try {
            resources = svnService.list(
                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromURL(srcUrl))),
                    SVNDepth.FILES,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            e.printStackTrace();
        }

        if(resources.isEmpty()){

        }

        //Checkout base project folder
        File wcFile = new File(workingCopyDirectoryConfig.getRootDirectory() + "/" + rootDirectoryEntry.get().getValue().getName());
        try {
            svnService.checkout(
                    new ArrayList<SvnTarget>(Arrays.asList(SvnTarget.fromURL(svnurl))),
                    SvnTarget.fromFile(wcFile),
                    SVNDepth.EMPTY,
                    SVNRevision.HEAD
            );
        } catch (SVNException e) {
            e.printStackTrace();
        }

        //Remote exists -> check if it's MFC project



//        SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
//        svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager());
//
//        //Check if repository exists
//        SvnGetInfo getInfo = svnOperationFactory.createGetInfo();
//        getInfo.setDepth(SVNDepth.EMPTY);   //Only check if exists
//        getInfo.setSingleTarget(SvnTarget.fromURL(svnurl));
//
//        SvnInfo remoteInfo;
//        try {
//             remoteInfo = getInfo.run();
//        } catch (SVNException e) {
//            throw new MfcProjectServiceException(e.getMessage());
//        }
//
//        if(remoteInfo != null){
//            // Check if MFC project
//            SvnList list = svnOperationFactory.createList();
//            list.setSingleTarget(SvnTarget.fromURL(svnurl));
//            list.setDepth(SVNDepth.IMMEDIATES);
//            list.setRevision(SVNRevision.HEAD);
//
//            List<String> extensions = new ArrayList();
//            extensions.add(".dsw");
//            extensions.add("sln");
//            AtomicReference<String> projectName = new AtomicReference<>();
//            list.setReceiver((svnTarget, svnDirEntry) -> {
//                for (String ext : extensions){
//                    if(svnDirEntry.getName().endsWith(ext)){
//                        projectName.set(svnDirEntry.getName());
//                        return;
//                    }
//                }
//            });
//
//            try {
//                list.run();
//            } catch (SVNException e) {
//                e.printStackTrace();
//            }
//
//            if(projectName != null){
//                // Check if working copy exists
//                File wc = new File(workingCopyDirectoryConfig.getRootDirectory() + "/" + projectName);
//                getInfo.setSingleTarget(SvnTarget.fromFile(wc));
//
//                SvnInfo wcInfo = null;
//                try {
//                    wcInfo = getInfo.run();
//                } catch (SVNException e) {
//                    e.printStackTrace();
//                }
//
//                if(wcInfo == null){
//                    // Checkout working copy
//                    SvnCheckout checkout = svnOperationFactory.createCheckout();
//                    checkout.setSingleTarget(SvnTarget.fromFile(wc));
//                    checkout.setDepth(SVNDepth.EMPTY);
//                    checkout.setSource(SvnTarget.fromURL(svnurl));
//
//                    try {
//                        checkout.run();
//                    } catch (SVNException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//
//
//        }
//
//
//        // Check
//        return null;

        return resultDto;
    }

    private SVNURL getSvnUrl(String url) throws MfcProjectServiceException {
        try {
            return SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            throw new MfcProjectServiceException(String.format("Cannot parse URL: {}", url));
        }
    }
//
//    @Override
//    public void remove(SvnItemDTO svnItemDTO) {
//
//    }
//
//    @Override
//    public SvnItemDTO update(SvnItemDTO svnItemDTO) {
//        return null;
//    }
//
//    @Override
//    public CommitDTO commit(CommitDTO commitDTO) {
//        return null;
//    }
}
