package de.atos.solumversion.components;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationManager {

    private ISVNAuthenticationManager authenticationManager;

    public AuthenticationManager() {
        clearCredentials();
    }

    public void setCredentials(String user, String password){
        this.authenticationManager = SVNWCUtil.createDefaultAuthenticationManager(user, password.toCharArray());
    }

    public void clearCredentials(){
        this.authenticationManager = SVNWCUtil.createDefaultAuthenticationManager();
    }

    public ISVNAuthenticationManager getAuthenticationManager(){
        return this.authenticationManager;
    }

}
