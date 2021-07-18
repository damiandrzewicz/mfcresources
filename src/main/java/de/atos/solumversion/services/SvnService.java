package de.atos.solumversion.services;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.util.List;
import java.util.Map;

public interface SvnService {

    void login(String user, String password);

    void logout();

    Map<SvnTarget, SvnInfo> info(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException;

    Map<SvnTarget, SVNDirEntry> list(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException;

    void checkout(List<SvnTarget> targets, SvnTarget source, SVNDepth depth, SVNRevision revision) throws SVNException;

    void update(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision) throws SVNException;

    Map<SvnTarget, SVNCommitInfo> commit(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, ISvnObjectReceiver<SVNCommitInfo> receiver) throws SVNException;

    void remove();

}
