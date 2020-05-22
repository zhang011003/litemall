package org.linlinjava.litemall.pay.bean.leshua;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaPayResponse extends BaseLeShuaResponse {

    @XStreamAlias("jspay_info")
    private String jspayInfo;

    @XStreamAlias("jspay_url")
    private String jspayUrl;

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
