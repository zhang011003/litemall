package org.linlinjava.litemall.pay.bean.leshua;

import org.linlinjava.litemall.db.util.OrderUtil;

import java.util.Arrays;

public enum LeShuaStatus {
    PAYING("0", OrderUtil.Status.STATUS_CREATE),
    PAID("2", OrderUtil.Status.STATUS_PAY),
    CANCELLED("6", OrderUtil.Status.STATUS_CANCEL),
    PAY_FAIL("8", OrderUtil.Status.STATUS_CREATE),
    REFUND("10", OrderUtil.Status.STATUS_REFUND),
    REFUND_CONFIRM("11", OrderUtil.Status.STATUS_REFUND_CONFIRM),
    REFUND_FAIL("12", OrderUtil.Status.STATUS_REFUND),
    ;

    private String status;
    private OrderUtil.Status orderStatus;
    LeShuaStatus(String status, OrderUtil.Status orderStatus) {
        this.status = status;
        this.orderStatus = orderStatus;
    }

    public String getStatus() {
        return status;
    }

    public OrderUtil.Status getOrderStatus() {
        return orderStatus;
    }

    @Override
    public String toString() {
        return super.toString() + "(status=" + getStatus() + ")";
    }

    public static LeShuaStatus getLeShuaStatus(String status) {
        return Arrays.stream(LeShuaStatus.values())
                .filter(s -> s.getStatus().equals(status))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}