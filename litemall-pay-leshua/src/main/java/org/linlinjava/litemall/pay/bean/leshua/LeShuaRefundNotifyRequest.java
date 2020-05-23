package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.springframework.util.StringUtils;

@XStreamAlias("leshua")
@Data
public class LeShuaRefundNotifyRequest extends BaseLeShuaResponse {
    @XStreamAlias("status")
    private String status;

    /**
     * 实际退款金额
     */
    @XStreamAlias("refund_amount")
    private String refundAmount;

    /**
     * 订单总金额
     */
    @XStreamAlias("total_amount")
    private String totalAmount;

    /**
     * 乐刷退款id
     */
    @XStreamAlias("leshua_refund_id")
    private String refundId;

    /**
     * 退款时间
     */
    @XStreamAlias("refund_time")
    private String refundTime;

    @XStreamAlias("failure_reason")
    private String failureReason;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }

    @Override
    protected String getKey(LeShuaProperties leShuaProperties) {
        return leShuaProperties.getNotifyKey();
    }
}
