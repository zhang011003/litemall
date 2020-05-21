package org.linlinjava.litemall.wx.leshua;

import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import org.linlinjava.litemall.db.util.OrderUtil;

import java.util.Arrays;

@XStreamAlias("leshua")
@Data
public class LeShuaRefundResponse extends BaseLeShuaResult {
    @XStreamAlias("status")
    private String status;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }
}
