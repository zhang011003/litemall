package org.linlinjava.litemall.admin.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.UnknownAccountException;
import org.linlinjava.litemall.admin.dto.AdminIntegration;
import org.linlinjava.litemall.admin.dto.AgentIntegration;
import org.linlinjava.litemall.core.config.IntegrationProperties;
import org.linlinjava.litemall.db.domain.Admin;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallAdminIntegration;
import org.linlinjava.litemall.db.service.LitemallAdminIntegrationService;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IntegrationService {
    @Autowired
    private LitemallAdminService adminService;
    @Autowired
    private IntegrationProperties integrationProperties;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LitemallAdminIntegrationService adminIntegrationService;

    public Admin integrationLogin(String userName, String password) {
        String loginUrl = integrationProperties.getLoginUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, String> body = Maps.newHashMap();
        body.put("username", userName);
        byte[] encrypt = new RSA(null, integrationProperties.getPublicKey()).encrypt(password, StandardCharsets.UTF_8, KeyType.PublicKey);
        body.put("password", Base64.encode(encrypt));

        log.info("使用集成方式进行登录，userName:{}", userName);

        HttpEntity<Map<String, String>> request = new HttpEntity(body, headers);
        ResponseEntity<AdminIntegration> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(loginUrl, request, AdminIntegration.class);
        } catch (RestClientException e) {
            log.error("", e);
            throw new UnknownAccountException("登录失败");
        }

        log.info("调用接口返回内容：{}", responseEntity.toString());

        Admin admin = null;
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AdminIntegration adminIntegration = responseEntity.getBody();
            List<LitemallAdmin> adminList = adminService.findAdmin(adminIntegration.getUser().getUsername());
            if (adminList.size() > 0) {
                LitemallAdmin litemallAdmin = adminList.get(0);
                // 如果头像不为空，需要更新头像。因为用户有可能是通过获取下挂的代理商方式添加的，获取下挂代理商是没有头像信息的
                if (!StringUtils.hasLength(litemallAdmin.getAvatar()) && StringUtils.hasLength(adminIntegration.getUser().getAvatar())) {
                    LitemallAdmin tempAdmin = new LitemallAdmin();
                    tempAdmin.setId(litemallAdmin.getId());
                    tempAdmin.setAvatar(adminIntegration.getUser().getAvatar());
                    adminService.updateById(tempAdmin);
                    litemallAdmin.setAvatar(adminIntegration.getUser().getAvatar());
                }
                LitemallAdminIntegration litemallAdminIntegration = adminIntegrationService.findById(litemallAdmin.getId());
                if (StringUtils.hasText(adminIntegration.getNamePaths())
                        && !adminIntegration.getNamePaths().equals(litemallAdminIntegration.getNamePath())) {
                    litemallAdminIntegration.setNamePath(adminIntegration.getNamePaths());
                    adminIntegrationService.updateById(litemallAdminIntegration);
                } else if (!StringUtils.hasText(adminIntegration.getNamePaths())
                        && StringUtils.hasText(litemallAdminIntegration.getNamePath())) {
                    litemallAdminIntegration.setNamePath("");
                    adminIntegrationService.updateById(litemallAdminIntegration);
                }
                admin = Admin.createAdmin(litemallAdmin, litemallAdminIntegration);
            } else {
                LitemallAdmin litemallAdmin = new LitemallAdmin();
                litemallAdmin.setUsername(adminIntegration.getUser().getUsername());
                litemallAdmin.setNickname(adminIntegration.getUser().getNickName());
                litemallAdmin.setPassword("");
                litemallAdmin.setDeleted(false);
                litemallAdmin.setAvatar(adminIntegration.getUser().getAvatar());
                LitemallAdminIntegration litemallAdminIntegration = new LitemallAdminIntegration();
                // level为空，说明是管理员，否则为代理商
                if (adminIntegration.getLevel() == null) {
                    litemallAdminIntegration.setType(Admin.Type.DEALER.getType());
                } else {
                    litemallAdminIntegration.setType(Admin.Type.AGENT.getType());
                    // 设一个空的parent
                    litemallAdmin.setParent(0);
                }
                Integer[] roleIds = adminIntegrationService.getRoleIds(litemallAdminIntegration.getType());
                litemallAdmin.setRoleIds(roleIds);
                adminService.add(litemallAdmin);
                Integer id = litemallAdmin.getId();
                litemallAdminIntegration.setId(id);
                litemallAdminIntegration.setNamePath(adminIntegration.getNamePaths());
                adminIntegrationService.add(litemallAdminIntegration);
                admin = Admin.createAdmin(litemallAdmin, litemallAdminIntegration);
            }

            admin.setBearerToken(adminIntegration.getToken());
        } else {
            throw new UnknownAccountException("登录失败");
        }

        return admin;
    }

    public List<LitemallAdmin> listOfMine(Integer loginUserId, Integer page, Integer limit, String sort, String order, String token) {
        List<LitemallAdmin> adminList = Lists.newArrayList();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", token);
        log.info("获取当前用户下挂的代理商, 用户id:{}", loginUserId);

        HttpEntity<String> entity = new HttpEntity(headers);
        ResponseEntity<AgentIntegration> res = null;
        try {
            res = restTemplate.exchange(integrationProperties.getAgentListPath(), HttpMethod.GET, entity, AgentIntegration.class);
        } catch (RestClientException e) {
            log.error("", e);
            return adminList;
        }
        log.info("获取当前用户下挂的代理商完成，用户id:{},返回值：{}", loginUserId, res);
        if (res.getStatusCode() == HttpStatus.OK) {
            AgentIntegration agentIntegration = res.getBody();
            adminList = Lists.newArrayListWithCapacity(agentIntegration.getContent().size());
            for (AgentIntegration.Agent agent : agentIntegration.getContent()) {
                List<LitemallAdmin> admins = adminService.findAdmin(agent.getUsername());

                // 如果存在集成的用户，则直接返回
                if (admins.size() > 0) {
                    adminList.add(admins.get(0));
                } else {
                    // 否则将该用户新增到数据库中，并返回
                    LitemallAdmin admin = new LitemallAdmin();
                    admin.setUsername(agent.getUsername());
                    admin.setNickname(agent.getNickName());
                    admin.setPassword("");
                    admin.setDeleted(false);
                    LitemallAdminIntegration litemallAdminIntegration = new LitemallAdminIntegration();
                    litemallAdminIntegration.setType(Admin.Type.AGENT.getType());
                    Integer[] roleIds = adminIntegrationService.getRoleIds(litemallAdminIntegration.getType());
                    admin.setRoleIds(roleIds);
                    admin.setParent(0);
                    adminService.add(admin);
                    Integer id = admin.getId();
                    litemallAdminIntegration.setId(id);
                    adminIntegrationService.add(litemallAdminIntegration);
                    adminList.add(admin);
                }
            }
        } else {
            log.error("获取当前用户下挂的代理商接口返回错误");
        }

        return adminList;
    }

    public boolean integrationTokenValid(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", token);
        log.info("校验token是否有效， token:{}", token);

        HttpEntity<String> entity = new HttpEntity(headers);
        ResponseEntity<String> res = null;
        try {
            res = restTemplate.exchange(integrationProperties.getCheckTokenPath(), HttpMethod.GET, entity, String.class);
        } catch (RestClientException e) {
            log.error("", e);
            return false;
        }
        log.info("校验token是否有效接口调用完成，token:{},返回值：{}", token, res);
        if (res.getStatusCode() == HttpStatus.OK) {
            return Boolean.valueOf(res.getBody());
        }
        return false;
    }
}
