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
import org.linlinjava.litemall.wx.service.WxOrderService;

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

        WxOrderService wxOrderService = BeanUtil.getBean(WxOrderService.class);

        LitemallOrder order = orderService.findById(this.orderId);
        if(order == null){
            return;
        }
        if(!OrderUtil.isCreateStatus(order)){
            return;
        }

        wxOrderService.closeOrder(order);

        logger.info("系统结束处理延时任务---订单超时未付款---" + this.orderId);
    }
}
