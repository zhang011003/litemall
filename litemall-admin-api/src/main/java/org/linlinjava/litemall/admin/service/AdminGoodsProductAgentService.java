package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.models.auth.In;
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

import java.math.BigDecimal;
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
    @Autowired
    private LitemallAccountService accountService;
    @Autowired
    private LitemallAccountHistoryService accountHistoryService;

    @Transactional
    public Object dispachProduct(Integer currentUserId, List<GoodsProductAgent> goodsProducts) {

        goodsProducts = merge(goodsProducts);

        Map<Integer, List<GoodsProductAgent>> agentIdMap = goodsProducts.stream().collect(
                Collectors.toMap(GoodsProductAgent::getAgentId,
                        Lists::newArrayList,
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        })
        );

        long adminCount = adminService.countAdmin(Lists.newArrayList(agentIdMap.keySet()));
        if (agentIdMap.size() != adminCount) {
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

        // 判断代理商余额是否足够
        for (Map.Entry<Integer, List<GoodsProductAgent>> entry : agentIdMap.entrySet()) {
            // 派发商品总价
            BigDecimal sum = entry.getValue().stream().map(agent -> agent.getDispatchPrice().multiply(new BigDecimal(agent.getDispatchNumber())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = accountService.findByAdminIdSelective(entry.getKey(), LitemallAccount.Column.balance).getBalance();
            if (balance.compareTo(sum) < 0) {
                String agentName = adminService.findById(entry.getKey()).getUsername();
                return ResponseUtil.fail(2, agentName + "账户没有足够额度扣减，需要充值金额：" + sum.subtract(balance));
            }
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

        this.afterDispatchGoods(goodsProducts);
        return ResponseUtil.ok();
    }


    /**
     * 合并相同的数据
     * @param goodsProducts
     * @return
     */
    private List<GoodsProductAgent> merge(List<GoodsProductAgent> goodsProducts) {
        Set<GoodsProductAgent> gpaSet = Sets.newHashSet();
        for (GoodsProductAgent goodsProduct : goodsProducts) {
            if (gpaSet.contains(goodsProduct)) {
                gpaSet.remove(goodsProduct);
                goodsProduct.setDispatchNumber(goodsProduct.getDispatchNumber() + 1);
            }
            gpaSet.add(goodsProduct);
        }

        return Lists.newArrayList(gpaSet);
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

    /**
     * 派货成功后的操作
     * @param goodsProductsAgent
     */
    private void afterDispatchGoods(List<GoodsProductAgent> goodsProductsAgent) {
        List<Integer> goodsIds = goodsProductsAgent.stream().map(GoodsProductAgent::getGoodsId).collect(Collectors.toList());
        List<LitemallGoods> goods = goodsService.findByIds(goodsIds, LitemallGoods.Column.id, LitemallGoods.Column.name);
        Map<Integer, LitemallGoods> goodsMap = goods.stream().collect(Collectors.toMap(LitemallGoods::getId, Function.identity()));

        List<Integer> goodsProductIds = goodsProductsAgent.stream().map(GoodsProductAgent::getGoodsProductId).collect(Collectors.toList());
        List<LitemallGoodsProduct> goodsProducts = goodsProductService.findByIds(goodsProductIds, LitemallGoodsProduct.Column.id, LitemallGoodsProduct.Column.specifications);
        Map<Integer, LitemallGoodsProduct> goodsProductMap = goodsProducts.stream().collect(Collectors.toMap(LitemallGoodsProduct::getId, Function.identity()));

        changeAccount(goodsProductsAgent, goodsMap, goodsProductMap);

        sendNotice(goodsProductsAgent, goodsMap, goodsProductMap);
    }

    /**
     * 修改账户余额以及账户变动历史
     * @param goodsProductsAgent
     * @param goodsMap
     * @param goodsProductMap
     */
    private void changeAccount(List<GoodsProductAgent> goodsProductsAgent,
                               Map<Integer, LitemallGoods> goodsMap,
                               Map<Integer, LitemallGoodsProduct> goodsProductMap) {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        for (int i = 0; i < goodsProductsAgent.size(); i++) {
            GoodsProductAgent goodsProductAgent = goodsProductsAgent.get(i);
            // 应支付金额=派货价格*库存
            BigDecimal money = goodsProductAgent.getDispatchPrice().multiply(new BigDecimal(goodsProductAgent.getDispatchNumber()));

            // 账户历史记录更新
            List<LitemallAccountHistory> historyList = Lists.newArrayListWithCapacity(2);
            LitemallAccountHistory history = new LitemallAccountHistory();
            history.setType((byte) 2);
            history.setMoney(money);
            LitemallAccount account = accountService.findByAdminIdSelective(
                    goodsProductAgent.getAgentId(), LitemallAccount.Column.balance);
            history.setBalance(account.getBalance().subtract(money));
            history.setAdminId(goodsProductAgent.getAgentId());

            String detail = String.format("派货成功扣款，派货方：%s, 商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    litemallAdmin.getUsername(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());
            history.setDetail(detail);
            historyList.add(history);

            history = new LitemallAccountHistory();
            history.setType((byte) 1);
            history.setMoney(money);
            account = accountService.findByAdminIdSelective(
                    litemallAdmin.getId(), LitemallAccount.Column.balance);
            history.setBalance(account.getBalance().add(money));
            history.setAdminId(litemallAdmin.getId());

            LitemallAdmin agentAdmin = adminService.findAdmin(goodsProductAgent.getAgentId(), LitemallAdmin.Column.id, LitemallAdmin.Column.username);
            detail = String.format("派货成功收款，收货方：%s, 商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    agentAdmin.getUsername(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());
            history.setDetail(detail);
            historyList.add(history);
            accountHistoryService.insertHistories(historyList, i);

            // 账户金额更新
            accountService.updateAccount(goodsProductAgent.getAgentId(),  money, false);
            accountService.updateAccount(litemallAdmin.getId(),  money, true);
        }
    }

    /**
     * 发送通知
     * @param goodsProductsAgent
     * @param goodsMap
     * @param goodsProductMap
     */
    private void sendNotice(List<GoodsProductAgent> goodsProductsAgent,
                            Map<Integer, LitemallGoods> goodsMap,
                            Map<Integer, LitemallGoodsProduct> goodsProductMap) {
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
