package org.linlinjava.litemall.wx.task;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.core.task.Task;
import org.linlinjava.litemall.core.util.BeanUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaQueryResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaStatus;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.service.LeShuaService;
import org.linlinjava.litemall.pay.util.LeShuaUtil;
import org.linlinjava.litemall.wx.service.WxOrderService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderStatusQueryTask extends Task {
    private int orderId = -1;

    public OrderStatusQueryTask(Integer orderId){
        super("OrderUnpaidQueryTask-" + orderId, TimeUnit.SECONDS.toMillis(10));
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始更新订单状态---orderId:{}", this.orderId);

        LitemallOrderService orderService = BeanUtil.getBean(LitemallOrderService.class);
        LeShuaService leShuaService = BeanUtil.getBean(LeShuaService.class);
        LeShuaProperties leShuaProperties = BeanUtil.getBean(LeShuaProperties.class);

        LitemallOrder order = orderService.findById(this.orderId);
        if(order == null){
            return;
        }
        if(!OrderUtil.isCreateStatus(order)){
            return;
        }

        boolean needUpdateStatus = false;
        boolean querySuccess = false;

        Short status = order.getOrderStatus();
        LocalDateTime localDateTime = null;
        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());
        switch (payType) {
            case LeShuaWeiXin:
                if (leShuaService == null) {
                    // 支付类型对应的service没有找到，该条记录的订单查询不处理
                    return;
                }
                LeShuaRequest leShuaRequest = LeShuaRequest.of(leShuaProperties.getQueryUrl())
                        .setService("query_status").setLeshuaOrderId(order.getPayId());
                String result = leShuaService.invoke(leShuaRequest);
                LeShuaQueryResponse leShuaQueryResponse = LeShuaUtil.fromXML(result, LeShuaQueryResponse.class);
                if (leShuaQueryResponse.isSuccess(leShuaProperties)) {
                    querySuccess = true;

                    // 更新订单状态
                    LeShuaStatus leShuaStatus = leShuaQueryResponse.getLeShuaStatus();
                    if (leShuaStatus == LeShuaStatus.PAID || leShuaStatus == LeShuaStatus.PAY_FAIL) {
                        needUpdateStatus = true;
                        status = leShuaStatus.getOrderStatus().getStatus();
                        localDateTime = LocalDateTime.parse(leShuaQueryResponse.getPayTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else {
                        log.warn("LeShua status does not equal to {} or {}, leshuaOrderId:{}, orderSn:{}",
                                LeShuaStatus.PAID.toString(),
                                LeShuaStatus.PAY_FAIL.toString(),
                                order.getPayId(), order.getOrderSn());
                    }
                }
                break;
            default:
                // TODO: 其它支付类型也需要查询状态
                needUpdateStatus = false;
                querySuccess = true;
                break;
        }

        if (needUpdateStatus) {
            // 更新订单状态
            order.setOrderStatus(status);
            order.setPayTime(localDateTime);
            if (orderService.updateWithOptimisticLocker(order) == 0) {
//            throw new RuntimeException("更新数据已失效");
                log.warn("订单数据状态更新失败， orderId={}", order.getId());
            }

            WxOrderService wxOrderService = BeanUtil.getBean(WxOrderService.class);
            wxOrderService.paySuccessPostHandler(order);
        }

        // 查询接口不成功，需要重新入队列更新状态
        if (!querySuccess){
            this.needReenterQueue = true;
            log.info("订单需要重新入队列更新状态，orderId:{}", order.getId());
        }

        log.info("系统完成更新订单状态---orderId:{}", this.orderId);
    }
}
