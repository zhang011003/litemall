package org.linlinjava.litemall.wx.task;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.core.system.SystemConfig;
import org.linlinjava.litemall.core.task.Task;
import org.linlinjava.litemall.core.util.BeanUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallOrderGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsProductService;
import org.linlinjava.litemall.db.service.LitemallOrderGoodsService;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaCloseResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.service.LeShuaService;
import org.linlinjava.litemall.pay.util.LeShuaUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderUnpaidTask extends Task {
    private final Log logger = LogFactory.getLog(OrderUnpaidTask.class);
    private int orderId = -1;

    public OrderUnpaidTask(Integer orderId, long delayInMilliseconds){
        super("OrderUnpaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnpaidTask(Integer orderId){
        super("OrderUnpaidTask-" + orderId, SystemConfig.getOrderUnpaid() * 60 * 1000);
        this.orderId = orderId;
    }

    @Override
    public void run() {
        logger.info("系统开始处理延时任务---订单超时未付款---" + this.orderId);

        LitemallOrderService orderService = BeanUtil.getBean(LitemallOrderService.class);
        LitemallOrderGoodsService orderGoodsService = BeanUtil.getBean(LitemallOrderGoodsService.class);
        LitemallGoodsProductService productService = BeanUtil.getBean(LitemallGoodsProductService.class);

        LeShuaService leShuaService = BeanUtil.getBean(LeShuaService.class);
        LeShuaProperties leShuaProperties = BeanUtil.getBean(LeShuaProperties.class);

        LitemallOrder order = orderService.findById(this.orderId);
        if(order == null){
            return;
        }
        if(!OrderUtil.isCreateStatus(order)){
            return;
        }

        //TODO: 其它支付是否需要关闭订单?
        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());
        switch (payType) {
            case LeShua:
                if (leShuaService == null) {
                    break;
                }
                Map<String, String> otherValueMap = Maps.newHashMap();
                otherValueMap.put("service", "close_order");

                LeShuaRequest leShuaRequest = LeShuaRequest.of(leShuaProperties.getCloseUrl())
                        .setService("close_order").setLeshuaOrderId(order.getPayId());
                String response = leShuaService.invoke(leShuaRequest);
                LeShuaCloseResponse leShuaCloseResponse = LeShuaUtil.fromXML(response, LeShuaCloseResponse.class);
                log.info("Order {}, leshua status: {}", order.getOrderSn(), leShuaCloseResponse.getLeShuaStatus().getStatus());
            default:
                break;

        }
        // 设置订单已取消状态
        order.setOrderStatus(OrderUtil.STATUS_AUTO_CANCEL);
        order.setEndTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        Integer orderId = order.getId();
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            Integer productId = orderGoods.getProductId();
            Short number = orderGoods.getNumber();
            if (productService.addStock(productId, number) == 0) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }
        logger.info("系统结束处理延时任务---订单超时未付款---" + this.orderId);
    }
}
