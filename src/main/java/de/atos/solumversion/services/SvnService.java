package de.atos.solumversion.services;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.ISvnObjectReceiver;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnStatus;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import java.util.List;
import java.util.Map;

public interface SvnService {

    void login(String user, String password);

    void logout();

    List<SvnInfo> info(SvnTarget target, SVNDepth depth, SVNRevision revision) throws SVNException;

    List<SVNDirEntry> list(SvnTarget target, SVNDepth depth, SVNRevision revision) throws SVNException;

    List<SvnStatus> status(SvnTarget target, SVNDepth depth, SVNRevision revision, boolean remote, boolean reportAll) throws SVNException;

    void checkout(SvnTarget target, SvnTarget source, SVNDepth depth, SVNRevision revision) throws SVNException;

    void update(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, boolean makeParents) throws SVNException;

    SVNCommitInfo commit(List<SvnTarget> targets, SVNDepth depth, SVNRevision revision, String message) throws SVNException;

    void cleanup(SvnTarget target, SVNDepth depth, SVNRevision revision) throws SVNException;

    void remove();

}
