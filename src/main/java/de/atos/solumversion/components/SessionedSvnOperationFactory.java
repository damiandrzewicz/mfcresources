package de.atos.solumversion.components;

import lombok.Getter;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.tmatesoft.svn.core.internal.wc17.SVNWCUtils;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;

@Component
@SessionScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionedSvnOperationFactory {

    private SvnOperationFactory svnOperationFactory;

    public SessionedSvnOperationFactory() {
        svnOperationFactory = new SvnOperationFactory();
        setDefaultAuth();
    }

    private void setDefaultAuth(){
        svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager());
    }

    public void setAuthentication(String user, String password){
        svnOperationFactory.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(user, password.toCharArray()));
    }

    public void clearAuthentication(){
        setDefaultAuth();
    }

    public SvnOperationFactory getFactory(){
        return svnOperationFactory;
    }
}
