package org.linlinjava.litemall.admin.shiro;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.linlinjava.litemall.admin.config.JWTToken;
import org.linlinjava.litemall.admin.service.IntegrationService;
import org.linlinjava.litemall.admin.util.JwtHelper;
import org.linlinjava.litemall.admin.util.JwtIntegrationHelper;
import org.linlinjava.litemall.core.config.IntegrationProperties;
import org.linlinjava.litemall.db.domain.Admin;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallAdminIntegration;
import org.linlinjava.litemall.db.service.LitemallAdminIntegrationService;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallPermissionService;
import org.linlinjava.litemall.db.service.LitemallRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@Slf4j
public class AdminAuthorizingRealm extends AuthorizingRealm {

    @Autowired
    private LitemallAdminService adminService;
    @Autowired
    private LitemallRoleService roleService;
    @Autowired
    private LitemallPermissionService permissionService;
    @Autowired
    private LitemallAdminIntegrationService adminIntegrationService;
    @Autowired
    private IntegrationProperties integrationProperties;
    @Autowired
    private IntegrationService integrationService;
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LitemallAdmin admin = (LitemallAdmin) getAvailablePrincipal(principals);
        Integer[] roleIds;
        if (admin instanceof Admin) {
            roleIds = adminIntegrationService.getRoleIds((Admin) admin);
        } else {
            roleIds = admin.getRoleIds();
        }
        Set<String> roles = roleService.queryByIds(roleIds);
        Set<String> permissions = permissionService.queryByRoleIds(roleIds);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(permissions);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String userName = null;
        if (integrationProperties.isEnable()) {
            if (integrationService.integrationTokenValid(token)) {
                userName = JwtIntegrationHelper.verifyTokenAndGetUserInfo(token.substring(7), "sub", String.class);
            }
        } else {
            userName = JwtHelper.verifyTokenAndGetUserInfo(token, "userName", String.class);
        }
        if (userName == null) {
            throw new AuthenticationException("token invalid");
        }

        List<LitemallAdmin> adminList = adminService.findAdmin(userName);
        if (adminList == null || adminList.size() != 1) {
            throw new AuthenticationException("User didn't existed!");
        }
        LitemallAdmin admin = adminList.get(0);
        LitemallAdminIntegration adminIntegration = adminIntegrationService.findById(admin.getId());
        if (adminIntegration != null) {
            return new SimpleAuthenticationInfo(Admin.createAdmin(admin, adminIntegration), token, "my_realm");
        } else {
            return new SimpleAuthenticationInfo(admin, token, "my_realm");
        }
    }
}
