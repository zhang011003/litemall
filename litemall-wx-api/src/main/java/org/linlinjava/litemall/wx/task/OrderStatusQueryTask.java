package org.linlinjava.litemall.wx.task;

import com.google.common.collect.Maps;
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

        //TODO: 需要判断是否是微信支付或乐刷支付，目前只支持乐刷支付后的查询场景
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

        Map<String, String> otherValueMap = Maps.newHashMap();
        otherValueMap.put("service", "query_status");

        String result = leShuaService.invoke(leShuaProperties.getQueryUrl(), order.getOrderSn(), "", otherValueMap);
        LeShuaQueryResponse leShuaQueryResponse = LeShuaPayResult.fromXML(result, LeShuaQueryResponse.class);

        boolean success = false;
        if (leShuaQueryResponse.isSuccess()) {
            // 更新订单状态
            LeShuaStatus leShuaStatus = leShuaQueryResponse.getLeShuaStatus();
            if (leShuaStatus != LeShuaStatus.PAYING ) {
                success = true;

                order.setOrderStatus(leShuaStatus.getOrderStatus());
                order.setEndTime(LocalDateTime.now());
                if (orderService.updateWithOptimisticLocker(order) == 0) {
//            throw new RuntimeException("更新数据已失效");
                    log.warn("订单数据状态更新失败， orderId={}", order.getId());
                }
            }
        }
        if (!success){
            // 需要重新入队列更新状态
            this.needReenterQueue = true;
            log.info("订单需要重新入队列更新状态，orderId={}", order.getId());
        }

        log.info("系统完成更新订单状态---" + this.orderId);
    }
}
