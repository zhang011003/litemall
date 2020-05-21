package org.linlinjava.litemall.wx.leshua;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaPayResponse extends BaseWxPayResult {
    @XStreamAlias("resp_code")
    protected String respCode;

    @XStreamAlias("result_code")
    protected String resultCode;

    @XStreamAlias("jspay_info")
    private String jspayInfo;

    @XStreamAlias("leshua_order_id")
    private String leshuaOrderId;

    private JSPayInfo jsPayInfo;

    public JSPayInfo getJsPayInfo() {
        if (jsPayInfo == null) {
            jsPayInfo = new Gson().fromJson(jspayInfo, JSPayInfo.class);
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jspayInfo).getAsJsonObject();
            jsPayInfo.packageValue = jsonObject.get("package").getAsString();
        }
        return jsPayInfo;
    }

    @Data
    public class JSPayInfo {
        private String appId;
        private String timeStamp;
        private String nonceStr;
        private String packageValue;
        private String signType;
        private String paySign;

    }
}
