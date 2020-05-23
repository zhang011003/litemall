package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaRefundResponse extends BaseLeShuaResponse {
    @XStreamAlias("status")
    private String status;

    /**
     * 实际退款金额
     */
    @XStreamAlias("settlement_refund_amount")
    private String refundAmount;

    /**
     * 订单总金额
     */
    @XStreamAlias("total_amount")
    private String totalAmount;

    /**
     * 订单余额
     */
    @XStreamAlias("order_balance")
    private String remainAmount;

    @XStreamAlias("leshua_refund_id")
    private String leshuaRefundId;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }
}
