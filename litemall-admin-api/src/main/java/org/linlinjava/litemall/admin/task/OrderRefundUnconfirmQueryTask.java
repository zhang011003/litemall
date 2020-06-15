package org.linlinjava.litemall.admin.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.SecurityManager;
import org.linlinjava.litemall.admin.service.AdminOrderService;
import org.linlinjava.litemall.admin.service.LogHelper;
import org.linlinjava.litemall.core.notify.NotifyService;
import org.linlinjava.litemall.core.notify.NotifyType;
import org.linlinjava.litemall.core.task.Task;
import org.linlinjava.litemall.core.util.BeanUtil;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.domain.LitemallOrderGoods;
import org.linlinjava.litemall.db.service.LitemallGoodsProductService;
import org.linlinjava.litemall.db.service.LitemallOrderGoodsService;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRefundQueryResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaStatus;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.service.LeShuaService;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        SecurityManager securityManager = BeanUtil.getBean(SecurityManager.class);
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
//            adminOrderService.refundPostHandler(orderId, order, refundAmount, "定时查询");
            refundPostHandler(orderId, order, refundAmount, "定时查询");
        }

        // 查询接口不成功，需要重新入队列更新状态
        if (!querySuccess){
            this.needReenterQueue = true;
            log.info("订单需要重新入队列更新状态，orderId={}", order.getId());
        }

        log.info("系统完成更新订单状态---" + this.orderId);
    }

    public void refundPostHandler(Integer orderId, LitemallOrder order, BigDecimal refundAmont, String comment) {
        LitemallOrderService orderService = BeanUtil.getBean(LitemallOrderService.class);
        LitemallOrderGoodsService orderGoodsService = BeanUtil.getBean(LitemallOrderGoodsService.class);
        LitemallGoodsProductService productService = BeanUtil.getBean(LitemallGoodsProductService.class);
        NotifyService notifyService = BeanUtil.getBean(NotifyService.class);
        LogHelper logHelper = BeanUtil.getBean(LogHelper.class);


        LocalDateTime now = LocalDateTime.now();
        // 设置订单取消状态
        order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
        order.setEndTime(now);
        // 记录订单退款相关信息
        order.setRefundAmount(refundAmont);

        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());
        String refundType = "";
        switch (payType) {
            case WeiXin:
                refundType = "微信退款接口";
                break;
            case LeShuaWeiXin:
                refundType = "乐刷微信退款接口";
                break;
            default:
                break;
        }
        order.setRefundType(refundType);
        order.setRefundTime(now);
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        List<LitemallOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
        for (LitemallOrderGoods orderGoods : orderGoodsList) {
            Integer productId = orderGoods.getProductId();
            Short number = orderGoods.getNumber();
            if (productService.addStock(productId, number, order.getAdminId()) == 0) {
                throw new RuntimeException("商品货品库存增加失败");
            }
        }

        //TODO 发送邮件和短信通知，这里采用异步发送
        // 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
        // 注意订单号只发后6位
        notifyService.notifySmsTemplate(order.getMobile(), NotifyType.REFUND,
                new String[]{order.getOrderSn().substring(8, 14)});

        logHelper.logOrderSucceed("退款", "订单编号 " + order.getOrderSn(), comment);
    }
}
