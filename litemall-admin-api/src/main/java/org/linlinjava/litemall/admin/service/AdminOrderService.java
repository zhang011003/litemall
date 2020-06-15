package org.linlinjava.litemall.admin.service;

import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.admin.dto.Order;
import org.linlinjava.litemall.admin.task.OrderRefundUnconfirmQueryTask;
import org.linlinjava.litemall.core.notify.NotifyService;
import org.linlinjava.litemall.core.notify.NotifyType;
import org.linlinjava.litemall.core.task.TaskService;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRefundNotifyRequest;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRefundResponse;
import org.linlinjava.litemall.pay.bean.leshua.LeShuaRequest;
import org.linlinjava.litemall.pay.properties.LeShuaProperties;
import org.linlinjava.litemall.pay.service.LeShuaService;
import org.linlinjava.litemall.pay.util.LeShuaUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.*;

@Service
@Slf4j
public class AdminOrderService {
    private final Log logger = LogFactory.getLog(AdminOrderService.class);

    @Autowired
    private LitemallOrderGoodsService orderGoodsService;
    @Autowired
    private LitemallOrderService orderService;
    @Autowired
    private LitemallGoodsProductService productService;
    @Autowired
    private LitemallUserService userService;
    @Autowired
    private LitemallCommentService commentService;
    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private LogHelper logHelper;
    @Autowired
    private TaskService taskService;
    @Autowired(required = false)
    private LeShuaService leShuaService;
    @Autowired(required = false)
    private LeShuaProperties leShuaProperties;
    @Autowired
    private LitemallAdminIntegrationService adminIntegrationService;
    public Object list(Integer userId, String orderSn, LocalDateTime start, LocalDateTime end, List<Short> orderStatusArray,
                       Integer page, Integer limit, String sort, String order) {
        List<LitemallOrder> orderList = orderService.querySelective(userId, orderSn, start, end, orderStatusArray, page, limit,
                sort, order);
        Set<Integer> adminIdSet = orderList.stream().map(LitemallOrder::getAdminId).collect(Collectors.toSet());
        List<LitemallAdminIntegration> adminIntegrationList = adminIntegrationService.findByIds(Lists.newArrayList(adminIdSet));
        Map<Integer, String> adminIdMap = adminIntegrationList.stream().collect(Collectors.toMap(LitemallAdminIntegration::getId, LitemallAdminIntegration::getNamePath));
        List<Order> orderDtoList = orderList.stream().map(o -> {
            Order orderDto = new Order();
            BeanUtils.copyProperties(o, orderDto);
            orderDto.setNamePath(adminIdMap.getOrDefault(o.getAdminId(), ""));
            return orderDto;
        }).collect(Collectors.toList());
        return ResponseUtil.okList(orderDtoList);
    }

    public Object detail(Integer id) {
        LitemallOrder order = orderService.findById(id);
        List<LitemallOrderGoods> orderGoods = orderGoodsService.queryByOid(id);
        UserVo user = userService.findUserVoById(order.getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("orderGoods", orderGoods);
        data.put("user", user);

        return ResponseUtil.ok(data);
    }

    /**
     * 订单退款
     * <p>
     * 1. 检测当前订单是否能够退款;
     * 2. 微信退款操作;
     * 3. 设置订单退款确认状态；
     * 4. 订单商品库存回库。
     * <p>
     * TODO
     * 虽然接入了微信退款API，但是从安全角度考虑，建议开发者删除这里微信退款代码，采用以下两步走步骤：
     * 1. 管理员登录微信官方支付平台点击退款操作进行退款
     * 2. 管理员登录litemall管理后台点击退款操作进行订单状态修改和商品库存回库
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单退款操作结果
     */
    @Transactional
    public Object refund(String body) {
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        String refundMoney = JacksonUtil.parseString(body, "refundMoney");
        if (orderId == null) {
            return ResponseUtil.badArgument();
        }
        if (StringUtils.isEmpty(refundMoney)) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }

        if (order.getActualPrice().compareTo(new BigDecimal(refundMoney)) != 0) {
            return ResponseUtil.badArgumentValue();
        }

        // 如果订单不是退款状态，则不能退款
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
            return ResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED, "订单不能确认收货");
        }

        // 元转成分
        Integer totalFee = order.getActualPrice().multiply(new BigDecimal(100)).intValue();
        WxPayRefundResult wxPayRefundResult = null;
        OrderUtil.PayType payType = OrderUtil.PayType.getPayType(order.getPayType());

        boolean refundFinish = false;
        switch (payType) {
            case WeiXin:
                // 微信退款
                WxPayRefundRequest wxPayRefundRequest = new WxPayRefundRequest();
                wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
                wxPayRefundRequest.setOutRefundNo("refund_" + order.getOrderSn());
                wxPayRefundRequest.setTotalFee(totalFee);
                wxPayRefundRequest.setRefundFee(totalFee);
                try {
                    wxPayRefundResult = wxPayService.refund(wxPayRefundRequest);
                } catch (WxPayException e) {
                    logger.error(e.getMessage(), e);
                    return ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败");
                }
                refundFinish = true;
                break;
            case LeShuaWeiXin:
                if (leShuaService == null) {
                    return ResponseUtil.fail(ORDER_REFUND_FAILED, "不支持乐刷退款");
                }
                LeShuaRequest leShuaRequest = LeShuaRequest.of(leShuaProperties.getRefundUrl())
                        .setService("unified_refund").setLeshuaOrderId(order.getPayId())
                        .setMerchantRefundId(order.getOrderSn())
                        .setRefundAmount(String.valueOf(totalFee))
                        .setNotifyUrl(leShuaProperties.getRefundNotifyUrl());
                LeShuaRefundResponse leShuaRefundResponse = leShuaService.invoke(leShuaRequest, LeShuaRefundResponse.class);
                wxPayRefundResult = new WxPayRefundResult();
                if (leShuaRefundResponse.isSuccess(leShuaProperties)) {
                    log.info("Order id: {}, leshua refund status:{}, refund amount:{}",
                            leShuaRefundResponse.getThirdOrderId(),
                            leShuaRefundResponse.getStatus(),
                            leShuaRefundResponse.getRefundAmount());
                    wxPayRefundResult.setReturnCode("SUCCESS");
                    wxPayRefundResult.setResultCode("SUCCESS");
                    wxPayRefundResult.setRefundId(leShuaRefundResponse.getLeshuaRefundId());
                } else {
                    wxPayRefundResult.setReturnCode("Failed");
                    wxPayRefundResult.setReturnMsg(leShuaRefundResponse.getErrorMsg());
                }
                refundFinish = false;
                break;
            default:
                break;
        }


        if (!wxPayRefundResult.getReturnCode().equals("SUCCESS")) {
            logger.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
            return ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败");
        }
        if (!wxPayRefundResult.getResultCode().equals("SUCCESS")) {
            logger.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
            return ResponseUtil.fail(ORDER_REFUND_FAILED, "订单退款失败");
        }

        order.setRefundContent(wxPayRefundResult.getRefundId());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            throw new RuntimeException("更新数据已失效");
        }

        // TODO：微信退款没有结果通知的调用？不需要查询是否退款成功？
        if (refundFinish) {
            order = orderService.findById(orderId);
            refundPostHandler(orderId, order, order.getActualPrice());
        } else {
            taskService.addTask(new OrderRefundUnconfirmQueryTask(orderId));
        }
        return ResponseUtil.ok();
    }

    /**
     * 退款成功后的操作
     * @param orderId
     * @param order
     * @return
     */
    public void refundPostHandler(Integer orderId, LitemallOrder order, BigDecimal refundAmont) {
        refundPostHandler(orderId, order, refundAmont, "");
    }

    public void refundPostHandler(Integer orderId, LitemallOrder order, BigDecimal refundAmont, String comment) {
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

    /**
     * 发货
     * 1. 检测当前订单是否能够发货
     * 2. 设置订单发货状态
     *
     * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    public Object ship(String body) {
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        String shipSn = JacksonUtil.parseString(body, "shipSn");
        String shipChannel = JacksonUtil.parseString(body, "shipChannel");
        if (orderId == null || shipSn == null || shipChannel == null) {
            return ResponseUtil.badArgument();
        }

        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }

        // 如果订单不是已付款状态，则不能发货
        if (!order.getOrderStatus().equals(OrderUtil.STATUS_PAY)) {
            return ResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED, "订单不能确认收货");
        }

        order.setOrderStatus(OrderUtil.STATUS_SHIP);
        order.setShipSn(shipSn);
        order.setShipChannel(shipChannel);
        order.setShipTime(LocalDateTime.now());
        if (orderService.updateWithOptimisticLocker(order) == 0) {
            return ResponseUtil.updatedDateExpired();
        }

        //TODO 发送邮件和短信通知，这里采用异步发送
        // 发货会发送通知短信给用户:          *
        // "您的订单已经发货，快递公司 {1}，快递单 {2} ，请注意查收"
        notifyService.notifySmsTemplate(order.getMobile(), NotifyType.SHIP, new String[]{shipChannel, shipSn});

        logHelper.logOrderSucceed("发货", "订单编号 " + order.getOrderSn());
        return ResponseUtil.ok();
    }

    /**
     * 删除订单
     * 1. 检测当前订单是否能够删除
     * 2. 删除订单
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    public Object delete(String body) {
        Integer orderId = JacksonUtil.parseInteger(body, "orderId");
        LitemallOrder order = orderService.findById(orderId);
        if (order == null) {
            return ResponseUtil.badArgument();
        }

        // 如果订单不是关闭状态(已取消、系统取消、已退款、用户已确认、系统已确认)，则不能删除
        Short status = order.getOrderStatus();
        if (!status.equals(OrderUtil.STATUS_CANCEL) && !status.equals(OrderUtil.STATUS_AUTO_CANCEL) &&
                !status.equals(OrderUtil.STATUS_CONFIRM) &&!status.equals(OrderUtil.STATUS_AUTO_CONFIRM) &&
                !status.equals(OrderUtil.STATUS_REFUND_CONFIRM)) {
            return ResponseUtil.fail(ORDER_DELETE_FAILED, "订单不能删除");
        }
        // 删除订单
        orderService.deleteById(orderId);
        // 删除订单商品
        orderGoodsService.deleteByOrderId(orderId);
        logHelper.logOrderSucceed("删除", "订单编号 " + order.getOrderSn());
        return ResponseUtil.ok();
    }

    /**
     * 回复订单商品
     *
     * @param body 订单信息，{ orderId：xxx }
     * @return 订单操作结果
     * 成功则 { errno: 0, errmsg: '成功' }
     * 失败则 { errno: XXX, errmsg: XXX }
     */
    public Object reply(String body) {
        Integer commentId = JacksonUtil.parseInteger(body, "commentId");
        if (commentId == null || commentId == 0) {
            return ResponseUtil.badArgument();
        }
        // 目前只支持回复一次
        LitemallComment comment = commentService.findById(commentId);
        if(comment == null){
            return ResponseUtil.badArgument();
        }
        if (!StringUtils.isEmpty(comment.getAdminContent())) {
            return ResponseUtil.fail(ORDER_REPLY_EXIST, "订单商品已回复！");
        }
        String content = JacksonUtil.parseString(body, "content");
        if (StringUtils.isEmpty(content)) {
            return ResponseUtil.badArgument();
        }
        // 更新评价回复
        comment.setAdminContent(content);
        commentService.updateById(comment);

        return ResponseUtil.ok();
    }

    public String refundNotifyLeShua(String body) {
        LeShuaRefundNotifyRequest refundNotifyRequest = LeShuaUtil.fromXML(body, LeShuaRefundNotifyRequest.class);
        if (refundNotifyRequest.isSuccess(leShuaProperties)) {
            LitemallOrder litemallOrder = orderService.findBySn(refundNotifyRequest.getThirdOrderId());
            refundPostHandler(litemallOrder.getId(), litemallOrder, new BigDecimal(refundNotifyRequest.getRefundAmount()), "消息通知");
            return "000000";
        } else {
            return "-1";
        }

    }
}
