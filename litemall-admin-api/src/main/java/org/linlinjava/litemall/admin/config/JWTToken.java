package org.linlinjava.litemall.admin.config;

import org.apache.shiro.authc.AuthenticationToken;

public class JWTToken implements AuthenticationToken {
    private String token;
    private String userName;
    public JWTToken(String userName, String token) {
        this.userName = userName;
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
