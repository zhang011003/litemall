package org.linlinjava.litemall.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "litemall.leshua")
@Data
public class LeShuaProperties {

    private boolean enable = false;

    private String urlBase;

    private String url;

    private String merchantId;

    private String key;

    private String notifyUrl;

    private String queryUrl;

    private String refundUrl;

    public void setUrl(String url) {
        this.url = getUrlBase() + url;
    }

    public void setQueryUrl(String queryUrl) {
        this.queryUrl = getUrlBase()  + queryUrl;
    }

    public void setRefundUrl(String refundUrl) {
        this.refundUrl = getUrlBase()+ refundUrl;
    }
}
