package org.linlinjava.litemall.admin.task;

import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.core.task.Task;
import org.linlinjava.litemall.core.util.BeanUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaQueryResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRefundQueryResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaStatus;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.service.LeShuaService;
import org.linlinjava.litemall.pay.util.LeShuaUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderRefundUnconfirmQueryTask extends Task {
    private int orderId = -1;

    public OrderRefundUnconfirmQueryTask(Integer orderId){
        super("OrderRefundUnconfirmQueryTask-" + orderId, TimeUnit.SECONDS.toMillis(10));
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始更新订单退款状态---" + this.orderId);

        LitemallOrderService orderService = BeanUtil.getBean(LitemallOrderService.class);
        AdminOrderService adminOrderService = BeanUtil.getBean(AdminOrderService.class);
        LeShuaService leShuaService = BeanUtil.getBean(LeShuaService.class);
        LeShuaProperties leShuaProperties = BeanUtil.getBean(LeShuaProperties.class);

        LitemallOrder order = orderService.findById(this.orderId);
        if(order == null){
            return;
        }
        if(!OrderUtil.isRefundStatus(order)){
            return;
        }

        boolean needUpdateStatus = false;
        boolean querySuccess = false;

        BigDecimal refundAmount = null;
        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());
        switch (payType) {
            case LeShuaWeiXin:
                if (leShuaService == null) {
                    // 支付类型对应的service没有找到，该条记录的订单查询不处理
                    querySuccess = true;
                    break;
                }
                if (order.getRefundContent() == null) {
                    log.info("Order id:{},leshua refund id is null, cannot make refund state query", order.getId());
                    querySuccess = true;
                    break;
                }
                LeShuaRequest leShuaRequest = LeShuaRequest.of(leShuaProperties.getRefundQueryUrl())
                        .setService("unified_query_refund").setLeshuaOrderId(order.getPayId())
                        .setLeshuaRefundId(order.getRefundContent());
                LeShuaRefundQueryResponse leShuaRefundQueryResponse = leShuaService.invoke(leShuaRequest, LeShuaRefundQueryResponse.class);
                if (leShuaRefundQueryResponse.isSuccess(leShuaProperties)) {
                    querySuccess = true;

                    // 更新订单状态
                    LeShuaStatus leShuaStatus = leShuaRefundQueryResponse.getLeShuaStatus();
                    if (leShuaStatus == LeShuaStatus.REFUND_CONFIRM) {
                        needUpdateStatus = true;
                        refundAmount = new BigDecimal(leShuaRefundQueryResponse.getSettlementRefundAmount());
                        log.info("Order id:{}, leshua refund id:{}, settlement refund amount:{}",
                                order.getId(), order.getRefundContent(),
                                leShuaRefundQueryResponse.getSettlementRefundAmount());
                    } else if (leShuaStatus == LeShuaStatus.REFUND_FAIL){
                        log.warn("Order id:{}, leshua refund id:{} refund failed",
                                order.getId(), order.getRefundContent());
                    }
                }
                break;
            default:
                // TODO: 其它支付类型也需要查询退款状态
                needUpdateStatus = false;
                querySuccess = true;
                break;
        }

        if (needUpdateStatus) {
            //TODO: 调用报shiro错误 No SecurityManager accessible to the calling code, either bound to the org.apache.shiro.util.ThreadContext or as a vm static singleton.  This is an invalid application configuration.
            adminOrderService.refundPostHandler(orderId, order, refundAmount, "定时查询");
        }

        // 查询接口不成功，需要重新入队列更新状态
        if (!querySuccess){
            this.needReenterQueue = true;
            log.info("订单需要重新入队列更新状态，orderId={}", order.getId());
        }

        log.info("系统完成更新订单状态---" + this.orderId);
    }
}
