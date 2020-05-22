package org.linlinjava.litemall.pay.bean.leshua;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.linlinjava.litemall.pay.bean.Param;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "of")
public class LeShuaRequest {
    @Param(ignore = true)
    @NonNull
    private String requestUrl;
    private String service;
    @Param("leshua_order_id")
    private String leshuaOrderId;
    @Param("third_order_id")
    private String orderSn;
    @Param("pay_way")
    private String payWay;
    @Param("merchant_id")
    private String merchantId;
    private String amount;
    @Param("jspay_flag")
    private String jspayFlag;
    @Param("jump_url")
    private String jumpUrl;
    @Param("notify_url")
    private String notifyUrl;
    @Param("refund_amount")
    private String refundAmount;
    @Param("merchant_refund_id")
    private String merchantRefundId;
    @Param("leshua_refund_id")
    private String leshuaRefundId;
}
