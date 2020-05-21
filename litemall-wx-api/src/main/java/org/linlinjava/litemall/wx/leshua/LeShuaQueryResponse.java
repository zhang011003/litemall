package org.linlinjava.litemall.wx.leshua;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import org.linlinjava.litemall.db.util.OrderUtil;

import java.util.Arrays;
import java.util.Objects;

@XStreamAlias("leshua")
@Data
public class LeShuaQueryResponse extends BaseLeShuaResult {
    @XStreamAlias("status")
    private String status;

    @XStreamAlias("out_transaction_id")
    private String transactionId;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }
}
