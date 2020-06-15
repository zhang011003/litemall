package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.dto.GoodsProductAgent;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.AccountUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import sun.management.resources.agent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    @Autowired
    private AdminGoodsDispatchHistoryService dispatchHistoryService;

    @Transactional
    public Object dispachProduct(Integer currentUserId, List<GoodsProductAgent> goodsProducts) {

//        goodsProducts = merge(goodsProducts);

        Map<Integer, List<GoodsProductAgent>> agentIdMap = goodsProducts.stream().collect(
                Collectors.toMap(GoodsProductAgent::getAgentId,
                        Lists::newArrayList,
                        (a, b) -> {
                            a.addAll(b);
                            return a;
                        })
        );

        long adminCount = adminService.findAdmin(Lists.newArrayList(agentIdMap.keySet()),
                LitemallAdmin.Column.id).size();
        if (agentIdMap.size() != adminCount) {
            // 通过agentId查询到的agent个数不对，说明参数传递错误
            log.error("通过agentId查询到的agent个数不对");
            return ResponseUtil.badArgument();
        }

        // 可能会有相同的goodsProductId传入,因为存在goodsProductId相同，但基础价不同的情况
        Set<Integer> gpIdSet = goodsProducts.stream().map(GoodsProductAgent::getGoodsProductId).collect(Collectors.toSet());
        // 判断goodsProductId参数在数据库中是否存在
        Map<Integer, LitemallGoodsProduct> goodsProductMap = goodsProductService.findByIds(
                Lists.newArrayList(gpIdSet)).stream().collect(
                        Collectors.toMap(LitemallGoodsProduct::getId, Function.identity()));
        if (goodsProductMap.keySet().size() < gpIdSet.size()) {
            log.error("传入的goodsProductId在数据库中有不存在的情况");
            return ResponseUtil.badArgument();
        }

        // 判断派货数量和派货价格
        for (GoodsProductAgent gp : goodsProducts) {
            LitemallGoodsProductAgent gpa = new LitemallGoodsProductAgent();
            gpa.setGoodsProductId(gp.getGoodsProductId());
            gpa.setDispatchPrice(gp.getDispatchPrice());
            gpa.setAgentId(currentUserId);
            List<LitemallGoodsProductAgent> gpas = goodsProductAgentService.queryByAgent(gpa);
            LitemallGoodsProductAgent mergedAgent = merge2(gpas).get(0);

            if (mergedAgent.getNumber() < gp.getDispatchNumber()) {
                return ResponseUtil.otherError("派货数量应小于库存数量");
            } else if (mergedAgent.getBasePrice() != null && gp.getDispatchPrice().compareTo(mergedAgent.getBasePrice()) < 0) {
                return ResponseUtil.otherError("派货价格应大于基础价");
            } else if (gp.getDispatchPrice().compareTo(mergedAgent.getPrice()) > 0) {
                return ResponseUtil.otherError("派货价格应小于售价");
            }
        }

//        // 判断代理商余额是否足够
//        for (Map.Entry<Integer, List<GoodsProductAgent>> entry : agentIdMap.entrySet()) {
//            // 派发商品总价
//            BigDecimal sum = entry.getValue().stream().map(agent -> agent.getDispatchPrice().multiply(new BigDecimal(agent.getDispatchNumber())))
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal balance = accountService.findByAdminIdAccountSelective(entry.getKey(), AccountUtil.AccountType.ACCOUNT, LitemallAccount.Column.balance).getBalance();
//            if (balance.compareTo(sum) < 0) {
//                String agentName = adminService.findById(entry.getKey()).getNickname();
//                return ResponseUtil.fail(2, agentName + "账户没有足够额度扣减，需要充值金额：" + sum.subtract(balance));
//            }
//        }

        LitemallAdmin admin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        goodsProducts.stream().forEach(goodsProduct -> {
            LitemallGoodsProductAgent gpa = new LitemallGoodsProductAgent();
            BeanUtils.copyProperties(goodsProduct, gpa);
            gpa.setParentAgentId(admin.getId());

            // 基础价格就是派货价格
            gpa.setBasePrice(gpa.getDispatchPrice());
            // 新数据的库存就是派货量
            gpa.setNumber(goodsProduct.getDispatchNumber());
            goodsProductAgentService.add(gpa);
            this.goodsProductService.reduceStock(gpa.getGoodsProductId(), goodsProduct.getDispatchNumber(), admin);
        });

        this.afterDispatchGoods(goodsProducts);
        return ResponseUtil.ok();
    }

    private List<LitemallGoodsProductAgent> merge2(List<LitemallGoodsProductAgent> gpas) {
        Map<String, LitemallGoodsProductAgent> mergeMap = gpas.stream().collect(Collectors.toMap(gpa -> gpa.getGoodsProductId() + "_" + gpa.getAgentId() + "_" + gpa.getDispatchPrice(), Function.identity(), (a, b) -> {
            a.setNumber(a.getNumber() + b.getNumber());
            return a;
        }));

        return Lists.newArrayList(mergeMap.values());
    }

    /**
     * 合并相同的数据
     * @param goodsProducts
     * @return
     */
    private List<GoodsProductAgent> merge(List<GoodsProductAgent> goodsProducts) {
        Map<String, GoodsProductAgent> mergeMap = goodsProducts.stream().collect(Collectors.toMap(GoodsProductAgent::toString, Function.identity(), (a, b) -> {
            a.setDispatchNumber(a.getDispatchNumber() + b.getDispatchNumber());
            return a;
        }));

        return Lists.newArrayList(mergeMap.values());
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

        saveHistory(goodsProductsAgent, goodsMap, goodsProductMap);

//        sendNotice(goodsProductsAgent, goodsMap, goodsProductMap);
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
        log.info("开始修改账户余额及账户变动历史");
        StopWatch stopWatch = new StopWatch("修改账户余额及账户变动历史");
        stopWatch.start();

        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        for (int i = 0; i < goodsProductsAgent.size(); i++) {
            GoodsProductAgent goodsProductAgent = goodsProductsAgent.get(i);
            // 应支付金额=派货价格*库存
            BigDecimal money = goodsProductAgent.getDispatchPrice().multiply(new BigDecimal(goodsProductAgent.getDispatchNumber()));

            // 账户历史记录更新
            List<LitemallAccountHistory> historyList = Lists.newArrayListWithCapacity(2);
            LitemallAccountHistory history = new LitemallAccountHistory();
            history.setType(AccountUtil.Type.OUTGOINGS.getType());
            history.setAccountType(AccountUtil.AccountType.ACCOUNT.getAccountType());
            history.setMoney(money);
            LitemallAccount account = accountService.findByAdminIdAccountSelective(
                    goodsProductAgent.getAgentId(), AccountUtil.AccountType.ACCOUNT, LitemallAccount.Column.balance);
            history.setBalance(account.getBalance().subtract(money));
            history.setAdminId(goodsProductAgent.getAgentId());

            String detail = String.format("派货成功扣款，派货方：%s, 商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    litemallAdmin.getNickname(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());
            history.setDetail(detail);
            historyList.add(history);

            history = new LitemallAccountHistory();
            history.setType(AccountUtil.Type.INCOME.getType());
            history.setAccountType(AccountUtil.AccountType.ACCOUNT.getAccountType());
            history.setMoney(money);
            account = accountService.findByAdminIdAccountSelective(
                    litemallAdmin.getId(), AccountUtil.AccountType.ACCOUNT, LitemallAccount.Column.balance);
            history.setBalance(account.getBalance().add(money));
            history.setAdminId(litemallAdmin.getId());

            LitemallAdmin agentAdmin = adminService.findAdmin(goodsProductAgent.getAgentId(), LitemallAdmin.Column.id, LitemallAdmin.Column.nickname);
            detail = String.format("派货成功收款，收货方：%s, 商品名称：%s, 货品规格：%s，派货价：%s，派货量：%s",
                    agentAdmin.getNickname(),
                    goodsMap.get(goodsProductAgent.getGoodsId()).getName(),
                    Arrays.toString(goodsProductMap.get(goodsProductAgent.getGoodsProductId()).getSpecifications()),
                    goodsProductAgent.getDispatchPrice(),
                    goodsProductAgent.getDispatchNumber());
            history.setDetail(detail);
            historyList.add(history);
            accountHistoryService.insertHistories(historyList, i);

            // 账户金额更新
            accountService.updateAccount(goodsProductAgent.getAgentId(), AccountUtil.AccountType.ACCOUNT, money, false);
            accountService.updateAccount(litemallAdmin.getId(), AccountUtil.AccountType.ACCOUNT, money, true);
        }
        stopWatch.stop();
        log.info(stopWatch.shortSummary());
    }

    private void saveHistory(List<GoodsProductAgent> goodsProductsAgent, Map<Integer, LitemallGoods> goodsMap, Map<Integer, LitemallGoodsProduct> goodsProductMap) {
        log.info("开始保存派货历史记录");
        StopWatch stopWatch = new StopWatch("保存派货历史记录");
        stopWatch.start();
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        for (int i = 0; i < goodsProductsAgent.size(); i++) {
            GoodsProductAgent goodsProductAgent = goodsProductsAgent.get(i);

            // 派货历史记录更新
            List<LitemallGoodsDispatchHistory> historyList = Lists.newArrayListWithCapacity(2);
            LitemallGoodsDispatchHistory history = new LitemallGoodsDispatchHistory();
            history.setAgentId(goodsProductAgent.getAgentId());
            history.setParentAgentId(litemallAdmin.getId());
            history.setGoodsId(goodsProductAgent.getGoodsId());
            history.setDispatchNumber(goodsProductAgent.getDispatchNumber());
            history.setDispatchPrice(goodsProductAgent.getDispatchPrice());
            history.setGoodsProductId(goodsProductAgent.getGoodsProductId());
            historyList.add(history);

            dispatchHistoryService.insertHistories(historyList, i);
        }
        stopWatch.stop();
        log.info(stopWatch.shortSummary());
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
                    litemallAdmin.getNickname(),
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
                    agentAdmin.getNickname(),
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
