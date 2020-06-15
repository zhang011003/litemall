package org.linlinjava.litemall.admin.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.dto.Goods;
import org.linlinjava.litemall.admin.dto.GoodsAllinone;
import org.linlinjava.litemall.admin.dto.GoodsDispatchHistory;
import org.linlinjava.litemall.admin.vo.CatVo;
import org.linlinjava.litemall.admin.vo.GoodsProductVo;
import org.linlinjava.litemall.core.qcode.QCodeService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.dao.LitemallGoodsDispatchHistoryMapper;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.GOODS_NAME_EXIST;

@Service
@Slf4j
public class AdminGoodsDispatchHistoryService {

    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsProductService goodsProductService;

    @Autowired
    private LitemallGoodsDispatchHistoryMapper dispatchHistoryMapper;
    @Autowired
    private LitemallAdminService adminService;

    public Object list(Goods goods, Integer type,
                       Integer page, Integer limit, String sort, String order) {
        if (type == null || (type != 1 && type != 2)) {
            return ResponseUtil.badArgumentValue();
        }
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        LitemallGoodsDispatchHistory dispatchHistory = new LitemallGoodsDispatchHistory();

        // 派货
        if (type == 1) {
            dispatchHistory.setParentAgentId(litemallAdmin.getId());
        } else {
            // 收货
            dispatchHistory.setAgentId(litemallAdmin.getId());
        }
        LitemallGoodsDispatchHistoryExample example = QueryUtil.constructExampleInstance(dispatchHistory, LitemallGoodsDispatchHistoryExample.class);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        PageHelper.startPage(page, limit);
        List<LitemallGoodsDispatchHistory> dispatchHistories = dispatchHistoryMapper.selectByExample(example);
        List<GoodsDispatchHistory> histories = Lists.newArrayListWithCapacity(dispatchHistories.size());

        if (dispatchHistories.size() > 0) {
            Set<Integer> goodsIds = dispatchHistories.stream().map(LitemallGoodsDispatchHistory::getGoodsId).collect(Collectors.toSet());

            List<LitemallGoods> litemallGoods = goodsService.queryByIds(
                    goodsIds.toArray(new Integer[goodsIds.size()]),
                    LitemallGoods.Column.id, LitemallGoods.Column.name, LitemallGoods.Column.picUrl);
            Set<Integer> goodsProductIds = dispatchHistories.stream().map(LitemallGoodsDispatchHistory::getGoodsProductId).collect(Collectors.toSet());
            List<LitemallGoodsProduct> litemallGoodsProducts = goodsProductService.findByIds(
                    Lists.newArrayList(goodsProductIds),
                    LitemallGoodsProduct.Column.id, LitemallGoodsProduct.Column.specifications);
            Map<Integer, LitemallGoods> goodsMap = litemallGoods.stream().collect(Collectors.toMap(LitemallGoods::getId, Function.identity()));
            Map<Integer, LitemallGoodsProduct> goodsProductMap = litemallGoodsProducts.stream().collect(Collectors.toMap(LitemallGoodsProduct::getId, Function.identity()));

            for (LitemallGoodsDispatchHistory tmpHistory : dispatchHistories) {
                GoodsDispatchHistory dto = new GoodsDispatchHistory();
                dto.setHistory(tmpHistory);
                dto.setGoods(goodsMap.get(tmpHistory.getGoodsId()));
                dto.setProduct(goodsProductMap.get(tmpHistory.getGoodsProductId()));
                if (type == 1) {
                    dto.setNickName(adminService.findAdmin(tmpHistory.getAgentId(), LitemallAdmin.Column.nickname).getNickname());
                } else {
                    dto.setNickName(adminService.findAdmin(tmpHistory.getParentAgentId(), LitemallAdmin.Column.nickname).getNickname());
                }
                histories.add(dto);
            }
        }
        return ResponseUtil.okList(histories);
    }

    public void insertHistories(List<LitemallGoodsDispatchHistory> historyList, int secondIncr) {
        for (LitemallGoodsDispatchHistory dispatchHistory : historyList) {
            dispatchHistory.setAddTime(LocalDateTime.now().plusSeconds(secondIncr));
            dispatchHistory.setUpdateTime(LocalDateTime.now().plusSeconds(secondIncr));
            dispatchHistory.setDeleted(false);
        }
        dispatchHistoryMapper.batchInsert(historyList);
    }
}
