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

    /**
     * 通用key
     */
    private String key;

    /**
     * 收到通知时获取sign用到的key
     */
    private String notifyKey;

    private String payNotifyUrl;

    private String queryUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String refundUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String refundQueryUrl = "cgi-bin/lepos_pay_gateway.cgi";

    private String refundNotifyUrl;

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

    public String getRefundQueryUrl() {
        return getUrl() + refundQueryUrl;
    }

    public String getCloseUrl() {
        return getUrl() + closeUrl;
    }
}
