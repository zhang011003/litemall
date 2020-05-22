package org.linlinjava.litemall.pay.bean.leshua;

import org.linlinjava.litemall.db.util.OrderUtil;

import java.util.Arrays;

public enum LeShuaStatus {
    PAYING("0", OrderUtil.STATUS_CREATE),
    PAID("2", OrderUtil.STATUS_PAY),
    CANCELLED("6", OrderUtil.STATUS_CANCEL),
    PAY_FAIL("8", OrderUtil.STATUS_CREATE),
    REFUND("10", OrderUtil.STATUS_REFUND),
    REFUND_CONFIRM("11", OrderUtil.STATUS_REFUND_CONFIRM),
    REFUND_FAIL("12", OrderUtil.STATUS_REFUND),
    DEFAULT("-1", (short)-1),
    ;

    private String status;
    private Short orderStatus;
    LeShuaStatus(String status, Short orderStatus) {
        this.status = status;
        this.orderStatus = orderStatus;
    }

    public String getStatus() {
        return status;
    }

    public Short getOrderStatus() {
        return orderStatus;
    }

    public static LeShuaStatus getLeShuaStatus(String status) {
        return Arrays.stream(LeShuaStatus.values())
                .filter(s -> s.getStatus().equals(status))
                .findFirst()
                .orElse(LeShuaStatus.DEFAULT);
    }
}