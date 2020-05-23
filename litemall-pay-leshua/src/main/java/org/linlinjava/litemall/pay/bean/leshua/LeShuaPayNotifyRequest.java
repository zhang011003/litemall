package org.linlinjava.litemall.pay.bean.leshua;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;

@XStreamAlias("leshua")
@Data
public class LeShuaPayNotifyRequest extends BaseLeShuaResponse {
    @XStreamAlias("amount")
    private String amount;
    @XStreamAlias("status")
    private String status;
    @XStreamAlias("pay_way")
    private String payWay;
    @XStreamAlias("pay_time")
    private String payTime;

    @Override
    protected String getKey(LeShuaProperties leShuaProperties) {
        return leShuaProperties.getNotifyKey();
    }
}