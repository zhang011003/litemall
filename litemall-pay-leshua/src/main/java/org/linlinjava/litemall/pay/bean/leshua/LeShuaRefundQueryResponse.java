package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaRefundQueryResponse extends BaseLeShuaResponse {
    @XStreamAlias("status")
    private String status;

    @XStreamAlias("settlement_refund_amount")
    private String settlementRefundAmount;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }
}
