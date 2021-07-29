package de.atos.solumversion.services;

import de.atos.solumversion.components.SessionedSvnOperationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc2.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class SvnServiceImpl implements SvnService{

    @Autowired
    private SessionedSvnOperationFactory operationFactory;

    public SvnServiceImpl() {
    }

    @Override
    public void login(String user, String password) {
        operationFactory.setAuthentication(user, password);
    }

    @Override
    public void logout() {
        operationFactory.clearAuthentication();
    }

    @Override
    public List<SvnInfo> info(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnGetInfo info = operationFactory.getFactory().createGetInfo();
        targets.stream().forEach(svnTarget -> info.addTarget(svnTarget));
        info.setDepth(depth);
        info.setRevision(revision);

        List<SvnInfo> result = new ArrayList();
        info.setReceiver((svnTarget, svnInfo) -> result.add(svnInfo));

        info.run();

        return result;
    }

    @Override
    public List<SVNDirEntry> list(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnList list = operationFactory.getFactory().createList();
        targets.stream().forEach(svnTarget -> list.addTarget(svnTarget));
        list.setDepth(depth);
        list.setRevision(revision);

        List<SVNDirEntry> result = new ArrayList();
        list.setReceiver((svnTarget, svnDirEntry) -> result.add(svnDirEntry));

        list.run();

        return result;
    }

    @Override
    public List<SvnStatus> status(SvnTarget target, SVNDepth depth, SVNRevision revision, boolean remote, boolean reportAll) throws SVNException {
        SvnGetStatus status = operationFactory.getFactory().createGetStatus();
        status.setSingleTarget(target);
        status.setDepth(depth);
        status.setRevision(revision);
        status.setRemote(remote);
        status.setReportAll(reportAll);

        List<SvnStatus> result = new ArrayList();
        status.setReceiver((svnTarget, svnStatus) -> result.add(svnStatus));

        status.run();

        long remoteRevision = status.getRemoteRevision();
       // status.getRevision()

        return result;
    }

    @Override
    public void checkout(SvnTarget target, SvnTarget source, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnCheckout checkout = operationFactory.getFactory().createCheckout();
        checkout.setSingleTarget(target);
        checkout.setSource(source);
        checkout.setDepth(depth);
        checkout.setRevision(revision);
        checkout.run();
    }

    @Override
    public void update(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision,  boolean makeParents) throws SVNException {
        SvnUpdate update = operationFactory.getFactory().createUpdate();
        targets.stream().forEach(svnTarget -> update.addTarget(svnTarget));
        update.setDepth(depth);
        update.setRevision(revision);
        update.setMakeParents(makeParents);

        update.run();
    }

    @Override
    public SVNCommitInfo commit(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, String message) throws SVNException {
        SvnCommit commit = operationFactory.getFactory().createCommit();
        targets.stream().forEach(svnTarget -> commit.addTarget(svnTarget));
        commit.setDepth(depth);
        commit.setRevision(revision);
        commit.setCommitMessage(message);

        return commit.run();
    }

    @Override
    public void cleanup(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnCleanup cleanup = operationFactory.getFactory().createCleanup();
        targets.stream().forEach(svnTarget -> cleanup.addTarget(svnTarget));
        cleanup.run();
    }

    @Override
    public void remove() {
        throw new RuntimeException("not implemented");
    }
}
