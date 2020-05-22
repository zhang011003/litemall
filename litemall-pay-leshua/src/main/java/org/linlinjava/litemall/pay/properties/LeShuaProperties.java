package org.linlinjava.litemall.pay.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "litemall.leshua")
public class LeShuaProperties {

    private String url = "https://paygate.leshuazf.com/";

    private String payUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String merchantId;

    private String key;

    private String notifyUrl;

    private String queryUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String refundUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String closeUrl = "cgi-bin/lepos_pay_gateway.cgi";

    public String getPayUrl() {
        return getUrl() + payUrl;
    }

    public String getQueryUrl() {
        return getUrl() + queryUrl;
    }

    public String getRefundUrl() {
        return getUrl() + refundUrl;
    }

    public String getCloseUrl() {
        return getUrl() + closeUrl;
    }
}
