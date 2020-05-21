package org.linlinjava.litemall.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "litemall.leshua")
@Data
public class LeShuaProperties {

    private boolean enable = false;

    private String url;

    private String merchantId;

    private String key;

    private String notifyUrl;
}
