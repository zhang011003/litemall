package org.linlinjava.litemall.wx.leshua;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaH5PayResponse extends BaseWxPayResult {
    @XStreamAlias("jspay_url")
    private String jspayUrl;

    @XStreamAlias("resp_code")
    protected int respCode;

    @XStreamAlias("result_code")
    protected String resultCode;

    @XStreamAlias("leshua_order_id")
    private String leshuaOrderId;

    public int getResultCodeInt() {
        return Integer.parseInt(resultCode);
    }
}
