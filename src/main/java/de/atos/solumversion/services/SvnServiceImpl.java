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
import org.tmatesoft.svn.core.wc2.*;

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
    public Map<SvnTarget, SvnInfo> info(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnGetInfo info = operationFactory.getFactory().createGetInfo();
        targets.stream().forEach(svnTarget -> info.addTarget(svnTarget));
        info.setDepth(depth);
        info.setRevision(revision);

        Map<SvnTarget, SvnInfo> map = new HashMap();
        info.setReceiver((svnTarget, svnInfo) -> map.put(svnTarget, svnInfo));

        info.run();

        return map;
    }

    @Override
    public Map<SvnTarget, SVNDirEntry> list(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnList list = operationFactory.getFactory().createList();
        targets.stream().forEach(svnTarget -> list.addTarget(svnTarget));
        list.setDepth(depth);
        list.setRevision(revision);

        Map<SvnTarget, SVNDirEntry> map = new HashMap();
        list.setReceiver((svnTarget, svnDirEntry) -> map.put(svnTarget, svnDirEntry));

        list.run();

        return map;
    }

    @Override
    public void checkout(List<SvnTarget> targets, SvnTarget source, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnCheckout checkout = operationFactory.getFactory().createCheckout();
        targets.stream().forEach(svnTarget -> checkout.addTarget(svnTarget));
        checkout.setSource(source);
        checkout.setDepth(depth);
        checkout.setRevision(revision);
        checkout.run();
    }

    @Override
    public void update(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException {
        SvnUpdate update = operationFactory.getFactory().createUpdate();
        targets.stream().forEach(svnTarget -> update.addTarget(svnTarget));
        update.setDepth(depth);
        update.setRevision(revision);
        update.run();
    }

    @Override
    public Map<SvnTarget, SVNCommitInfo> commit(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, ISvnObjectReceiver<SVNCommitInfo> receiver) throws SVNException {
        SvnCommit commit = operationFactory.getFactory().createCommit();
        targets.stream().forEach(svnTarget -> commit.addTarget(svnTarget));
        commit.setDepth(depth);
        commit.setRevision(revision);

        Map<SvnTarget, SVNCommitInfo> map = new HashMap();
        commit.setReceiver((svnTarget, commitInfo) -> map.put(svnTarget, commitInfo));

        commit.run();

        return map;
    }

    @Override
    public void remove() {
        throw new RuntimeException("not implemented");
    }
}
