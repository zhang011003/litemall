package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.dto.Goods;
import org.linlinjava.litemall.admin.dto.GoodsProductAgent;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminGoodsProductAgentService {

    @Autowired
    private LitemallAdminService adminService;
    @Autowired
    private LitemallGoodsProductService goodsProductService;
    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsProductAgentService goodsProductAgentService;
    @Autowired
    private LitemallNoticeService noticeService;
    @Autowired
    private LitemallNoticeAdminService noticeAdminService;

    @Transactional
    public Object dispachProduct(Integer currentUserId, List<GoodsProductAgent> goodsProducts) {

        Set<Integer> adminIds = goodsProducts.stream().map(GoodsProductAgent::getAgentId).collect(Collectors.toSet());
        long adminCount = adminService.countAdmin(Lists.newArrayList(adminIds));
        if (adminIds.size() != adminCount) {
            // 通过agentId查询到的agent个数不对，说明参数传递错误
            return ResponseUtil.badArgument();
        }

        // 可能会有相同的goodsProductId传入,因为存在goodsProductId相同，但基础价不同的情况
        Set<Integer> gpIdSet = goodsProducts.stream().map(GoodsProductAgent::getGoodsProductId).collect(Collectors.toSet());
        // 判断goodsProductId参数在数据库中是否存在
        Map<Integer, LitemallGoodsProduct> goodsProductMap = gpIdSet.stream()
                .map(goodsProductService::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LitemallGoodsProduct::getId,
                        Function.identity()));
        if (goodsProductMap.keySet().size() < gpIdSet.size()) {
            return ResponseUtil.badArgument();
        }

        Map<LitemallGoodsProductAgent, LitemallGoodsProductAgent> gpaMap = Maps.newHashMap();

        // 判断派货数量和派货价格
        for (GoodsProductAgent gp : goodsProducts) {
            LitemallGoodsProductAgent gpa = new LitemallGoodsProductAgent();
            BeanUtils.copyProperties(gp, gpa);
            gpa.setAgentId(currentUserId);
            List<LitemallGoodsProductAgent> gpas = goodsProductAgentService.queryByAgent(gpa);
            if (gpas.size() > 0) {
                if (gpas.get(0).getNumber() < gp.getDispatchNumber()) {
                    return ResponseUtil.fail(1, "派货数量应小于库存数量");
                } else if (gpas.get(0).getBasePrice().compareTo(gp.getDispatchPrice()) > 0) {
                    return ResponseUtil.fail(1, "派货价格应小于基础价");
                } else {
                    LitemallGoodsProductAgent goodsProductAgent = constructGoodsProductAgent(currentUserId, goodsProductMap, gp);
                    gpaMap.put(goodsProductAgent, gpas.get(0));
                }
            } else {
                if (goodsProductMap.get(gp.getGoodsProductId()).getNumber() < gp.getDispatchNumber()) {
                    return ResponseUtil.fail(1, "派货数量应小于库存数量");
                } else {
                    LitemallGoodsProductAgent goodsProductAgent = constructGoodsProductAgent(currentUserId, goodsProductMap, gp);
                    gpaMap.put(goodsProductAgent, null);
                }
            }
        }

        // 判断派货价格是否大于售价
        List<GoodsProductAgent> tmpGoodsProducts = goodsProducts.stream().filter(g -> g.getDispatchPrice().compareTo(goodsProductMap.get(g.getGoodsProductId()).getPrice()) < 0)
                .collect(Collectors.toList());
        if (tmpGoodsProducts.size() < goodsProducts.size()) {
            return ResponseUtil.badArgument();
        }

        for (Map.Entry<LitemallGoodsProductAgent, LitemallGoodsProductAgent> entry : gpaMap.entrySet()) {
            LitemallGoodsProductAgent agent = entry.getKey();
            goodsProductAgentService.add(agent);
            LitemallGoodsProductAgent parentAgent = entry.getValue();
            if (parentAgent != null) {
                this.goodsProductAgentService.reduceStock(parentAgent.getId(), agent.getNumber());
            } else {
                this.goodsProductService.reduceStock(agent.getGoodsProductId(), agent.getNumber());
            }
        }
        this.sendNotice(goodsProducts);
        return ResponseUtil.ok();
    }

    private LitemallGoodsProductAgent constructGoodsProductAgent(Integer currentUserId, Map<Integer, LitemallGoodsProduct> goodsProductMap, GoodsProductAgent gp) {
        LitemallGoodsProductAgent goodsProductAgent = new LitemallGoodsProductAgent();
        BeanUtils.copyProperties(goodsProductMap.get(gp.getGoodsProductId()), goodsProductAgent,"id", "addTime", "updateTime");
        BeanUtils.copyProperties(gp, goodsProductAgent);
        goodsProductAgent.setNumber(gp.getDispatchNumber());
        goodsProductAgent.setParentAgentId(currentUserId);
        // 传入的派货价格就是新数据的基础价格
        goodsProductAgent.setBasePrice(gp.getDispatchPrice());
        return goodsProductAgent;
    }

    private void sendNotice(List<GoodsProductAgent> goodsProductsAgent) {
        List<Integer> goodsIds = goodsProductsAgent.stream().map(GoodsProductAgent::getGoodsId).collect(Collectors.toList());
        List<LitemallGoods> goods = goodsService.findByIds(goodsIds, LitemallGoods.Column.id, LitemallGoods.Column.name);
        Map<Integer, LitemallGoods> goodsMap = goods.stream().collect(Collectors.toMap(LitemallGoods::getId, Function.identity()));

        List<Integer> goodsProductIds = goodsProductsAgent.stream().map(GoodsProductAgent::getGoodsProductId).collect(Collectors.toList());
        List<LitemallGoodsProduct> goodsProducts = goodsProductService.findByIds(goodsProductIds, LitemallGoodsProduct.Column.id, LitemallGoodsProduct.Column.specifications);
        Map<Integer, LitemallGoodsProduct> goodsProductMap = goodsProducts.stream().collect(Collectors.toMap(LitemallGoodsProduct::getId, Function.identity()));

        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        for (GoodsProductAgent goodsProductAgent : goodsProductsAgent) {
            // 收货人收到通知
            LitemallNotice notice = new LitemallNotice();
            notice.setTitle("派货成功");
            notice.setAdminId(0);
            String content = String.format("%s向您派货成功，商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    litemallAdmin.getUsername(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());

            notice.setContent(content);
            notice.setAddTime(LocalDateTime.now());
            notice.setUpdateTime(LocalDateTime.now());
            noticeService.add(notice);

            LitemallNoticeAdmin noticeAdmin = new LitemallNoticeAdmin();
            noticeAdmin.setNoticeId(notice.getId());
            noticeAdmin.setNoticeTitle(notice.getTitle());
            noticeAdmin.setAdminId(goodsProductAgent.getAgentId());
            noticeAdmin.setAddTime(LocalDateTime.now());
            noticeAdmin.setUpdateTime(LocalDateTime.now());
            noticeAdminService.add(noticeAdmin);

            // 派货人收到通知
            LitemallAdmin agentAdmin = adminService.findAdmin(goodsProductAgent.getAgentId(), LitemallAdmin.Column.id, LitemallAdmin.Column.username);
            String content2 = String.format("您向%s派货成功，商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    agentAdmin.getUsername(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());
            notice.setContent(content2);
            noticeService.add(notice);
            noticeAdmin.setNoticeId(notice.getId());
            noticeAdmin.setAdminId(litemallAdmin.getId());
            noticeAdminService.add(noticeAdmin);
        }
    }
}
