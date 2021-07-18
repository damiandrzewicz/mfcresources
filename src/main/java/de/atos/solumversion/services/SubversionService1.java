package de.atos.solumversion.services;

import org.springframework.stereotype.Service;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.dav.http.DefaultHTTPConnectionFactory;
import org.tmatesoft.svn.core.internal.io.dav.http.IHTTPConnectionFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.*;

@Service
public class SubversionService1 {

    private SVNRepository repository = null;
    private ISVNAuthenticationManager authenticationManager = null;
    private SVNClientManager svnClientManager;
    private ISVNOptions options = null;

    SubversionService1(){

        /*
         * For using over http:// and https://
         */
        IHTTPConnectionFactory factory =
                new DefaultHTTPConnectionFactory(null, true, null);
        DAVRepositoryFactory.setup(factory);

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();

        this.authenticationManager = SVNWCUtil.createDefaultAuthenticationManager();

        this.options = SVNWCUtil.createDefaultOptions(true);
        this.svnClientManager = SVNClientManager.newInstance(options, authenticationManager);
    }

    public void authenticate(String user, String password){
        this.authenticationManager = new BasicAuthenticationManager(user, password);
        this.svnClientManager = SVNClientManager.newInstance(options, this.authenticationManager);
    }

    public void open(SVNURL url) throws SVNException, SVNAuthenticationException {
        this.repository = SVNRepositoryFactory.create(url);
        this.repository.setAuthenticationManager(authenticationManager);

        this.repository.testConnection();
    }


    public Collection getDirEntries(String path, SVNRevision revision) throws SVNException {
        Objects.requireNonNull(this.repository);
        return this.repository.getDir(path, revision.getNumber(), null, (Collection) null);
    }


    public long checkoutFolder(SVNURL url, SVNRevision revision, File destPath, SVNDepth svnDepth) throws SVNException {
        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doCheckout(url, destPath, revision, revision, svnDepth, false);
    }

    public long[] updateFiles(File[] path, SVNRevision revision, SVNDepth svnDepth) throws SVNException {
        SVNUpdateClient updateClient = svnClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(path, revision, svnDepth, false, false, true);
    }


    public SVNCommitInfo commit(File[] wcPath, boolean keepLocks, String commitMessage) throws SVNException {
        return svnClientManager
                .getCommitClient()
                .doCommit(wcPath, keepLocks, commitMessage, null, null, true, false, SVNDepth.EMPTY);
    }

    public List<File> listModifiedFiles(File path, SVNRevision revision) throws SVNException {
        final List<File> fileList = new ArrayList<File>();
        SVNStatusClient statusClient = svnClientManager.getStatusClient();
        statusClient.doStatus(path, revision, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
            @Override
            public void handleStatus(SVNStatus status) throws SVNException {
                SVNStatusType statusType = status.getContentsStatus();
                if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL
                        && statusType != SVNStatusType.STATUS_IGNORED) {
                    fileList.add(status.getFile());
                }
            }
        }, null);
        return fileList;
    }

    public SVNInfo wcInfo(File path, SVNRevision revision) throws SVNException {
        SVNWCClient wcClient = svnClientManager.getWCClient();
        return wcClient.doInfo(path, revision);
    }
}
