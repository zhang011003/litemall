package org.linlinjava.litemall.pay.bean.leshua;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@XStreamAlias("leshua")
@Data
public class LeShuaQueryResponse extends BaseLeShuaResponse {
    @XStreamAlias("status")
    private String status;

    @XStreamAlias("out_transaction_id")
    private String transactionId;
    @XStreamAlias("pay_time")
    private String payTime;

    public LeShuaStatus getLeShuaStatus() {
        return LeShuaStatus.getLeShuaStatus(getStatus());
    }
}
