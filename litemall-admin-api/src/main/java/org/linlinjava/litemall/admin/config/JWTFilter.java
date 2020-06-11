package org.linlinjava.litemall.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.linlinjava.litemall.admin.util.Consts;
import org.linlinjava.litemall.admin.util.JwtHelper;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JWTFilter extends BasicHttpAuthenticationFilter {

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info("Invoke executeLogin,{}", req.getRequestURI());
        String adminToken = req.getHeader(Consts.HEADER_ADMIN_TOKEN);
        String userName = JwtHelper.verifyTokenAndGetUserInfo(adminToken, "userName", String.class);
        JWTToken token = new JWTToken(userName, adminToken);
        getSubject(request, response).login(token);
        return true;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        log.info("Invoke isAccessAllowed,{}", req.getRequestURI());
        if (isLoginAttempt(request, response)) {
            try {
                executeLogin(request, response);
                return true;
            } catch (Exception e) {
                log.error("", e);
                response401(request, response);
            }
        }
        return true;
    }

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(Consts.HEADER_ADMIN_TOKEN);
        return authorization != null;
    }

    private void response401(ServletRequest req, ServletResponse resp) {
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
            httpServletResponse.sendRedirect("/admin/auth/401");
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
