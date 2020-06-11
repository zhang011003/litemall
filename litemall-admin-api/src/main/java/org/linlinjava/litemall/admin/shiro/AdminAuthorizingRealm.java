package org.linlinjava.litemall.admin.shiro;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.linlinjava.litemall.admin.config.JWTToken;
import org.linlinjava.litemall.admin.util.JwtHelper;
import org.linlinjava.litemall.db.domain.Admin;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.service.LitemallAdminIntegrationService;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallPermissionService;
import org.linlinjava.litemall.db.service.LitemallRoleService;
import org.springframework.beans.factory.annotation.Autowired;

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
//    @Autowired
//    private IntegrationProperties integrationProperties;
    @Autowired
    private LitemallAdminIntegrationService adminIntegrationService;
//    @Autowired
//    private RestTemplate restTemplate;

//    public AdminAuthorizingRealm(CacheManager cacheManager) {
//        super(cacheManager);
//        this.setAuthenticationCachingEnabled(true);
//    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        return ((JWTToken)token).getUserName();
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

//        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
//        String username = upToken.getUsername();
//        String password = new String(upToken.getPassword());
//
//        if (StringUtils.isEmpty(username)) {
//            throw new AccountException("用户名不能为空");
//        }
//        if (StringUtils.isEmpty(password)) {
//            throw new AccountException("密码不能为空");
//        }
//
//        LitemallAdmin admin;
//        if (integrationProperties.isEnable()) {
//            admin = integrationLogin(username, password);
//        } else {
//            List<LitemallAdmin> adminList = adminService.findAdmin(username);
//            Assert.state(adminList.size() < 2, "同一个用户名存在两个账户");
//            if (adminList.size() == 0) {
//                throw new UnknownAccountException("找不到用户（" + username + "）的帐号信息");
//            }
//
//            admin = adminList.get(0);
//
//            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//            if (!encoder.matches(password, admin.getPassword())) {
//                throw new UnknownAccountException("找不到用户（" + username + "）的帐号信息");
//            }
//        }
//
//        return new SimpleAuthenticationInfo(admin, password, getName());

        String token = (String) authenticationToken.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String userName = JwtHelper.verifyTokenAndGetUserInfo(token, "userName", String.class);
        if (userName == null) {
            throw new AuthenticationException("token invalid");
        }

        List<LitemallAdmin> adminList = adminService.findAdmin(userName);
        if (adminList == null || adminList.size() != 1) {
            throw new AuthenticationException("User didn't existed!");
        }

        return new SimpleAuthenticationInfo(adminList.get(0), token, "my_realm");
    }
}
