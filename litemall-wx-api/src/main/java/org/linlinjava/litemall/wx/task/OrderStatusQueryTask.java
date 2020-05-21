package org.linlinjava.litemall.wx.task;

import com.google.common.collect.Maps;
import com.qcloud.cos.transfer.PauseStatus;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.core.config.LeShuaProperties;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.core.task.Task;
import org.linlinjava.litemall.core.util.BeanUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallOrderGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsProductService;
import org.linlinjava.litemall.db.service.LitemallOrderGoodsService;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.wx.leshua.LeShuaPayResult;
import org.linlinjava.litemall.wx.leshua.LeShuaQueryResponse;
import org.linlinjava.litemall.wx.leshua.LeShuaStatus;
import org.linlinjava.litemall.wx.service.LeShuaService;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderStatusQueryTask extends Task {
    private int orderId = -1;

    public OrderStatusQueryTask(Integer orderId, long delayInMilliseconds){
        super("OrderUnpaidQueryTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderStatusQueryTask(Integer orderId){
        super("OrderUnpaidQueryTask-" + orderId, TimeUnit.SECONDS.toMillis(2));
        this.orderId = orderId;
    }

    @Override
    public void run() {
        log.info("系统开始更新订单状态---" + this.orderId);

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
        String payId = order.getPayId();

        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());
        switch (payType) {
            case LeShua:
                Map<String, String> otherValueMap = Maps.newHashMap();
                otherValueMap.put("service", "query_status");

                String result = leShuaService.invoke(leShuaProperties.getQueryUrl(), order.getOrderSn(), "", otherValueMap);
                LeShuaQueryResponse leShuaQueryResponse = LeShuaPayResult.fromXML(result, LeShuaQueryResponse.class);
                if (leShuaQueryResponse.isSuccess()) {
                    querySuccess = true;

                    // 更新订单状态
                    LeShuaStatus leShuaStatus = leShuaQueryResponse.getLeShuaStatus();
                    if (leShuaStatus != LeShuaStatus.PAYING) {
                        needUpdateStatus = true;
                        status = leShuaStatus.getOrderStatus();
                        payId = leShuaQueryResponse.getTransactionId();
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
            order.setPayId(payId);
            order.setEndTime(LocalDateTime.now());
            if (orderService.updateWithOptimisticLocker(order) == 0) {
//            throw new RuntimeException("更新数据已失效");
                log.warn("订单数据状态更新失败， orderId={}", order.getId());
            }
        }

        // 查询接口不成功，需要重新入队列更新状态
        if (!querySuccess){
            this.needReenterQueue = true;
            log.info("订单需要重新入队列更新状态，orderId={}", order.getId());
        }

        log.info("系统完成更新订单状态---" + this.orderId);
    }
}
