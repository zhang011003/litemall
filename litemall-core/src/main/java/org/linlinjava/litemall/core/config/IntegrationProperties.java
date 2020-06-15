package org.linlinjava.litemall.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "litemall.integration")
@Data
public class IntegrationProperties {
    private Boolean enable;
    private String url;
    private String publicKey;
    private String jwtSecret;
    private static final String loginPath = "/auth/remoteLogin";
    private static final String agentListPath = "/api/agent/list";
    private static final String checkTokenPath = "/auth/checkToken";
    public boolean isEnable() {
        return enable != null && enable;
    }
    public String getLoginUrl() {
        return getUrl() + loginPath;
    }
    public String getAgentListPath() {
        return getUrl() + agentListPath;
    }

    public String getCheckTokenPath() {
        return getUrl() + checkTokenPath;
    }
}
