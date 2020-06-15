package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.db.dao.LitemallGoodsMapper;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallGoods.Column;
import org.linlinjava.litemall.db.domain.LitemallGoodsExample;
import org.linlinjava.litemall.db.domain.LitemallGoodsExtraInfo;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.util.AgentHolder;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LitemallGoodsService {
    Column[] columns = new Column[]{Column.id, Column.name, Column.brief, Column.picUrl, Column.isHot, Column.isNew, Column.counterPrice, Column.retailPrice};
    @Resource
    private LitemallGoodsMapper goodsMapper;
    @Resource
    private GoodsAgentService goodsAgentService;
    @Resource
    private LitemallGoodsExtraInfoService goodsExtraInfoService;
    @Resource
    private LitemallGoodsProductAgentService gpaService;

    /**
     * 获取热卖商品
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByHot(int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria = example.or().andIsHotEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        List<Integer> goodsIds = goodsAgentService.getGoodsIds();
        if (goodsIds.size() > 0) {
            criteria.andIdIn(goodsIds);
        }
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 获取新品上市
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByNew(int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria = example.or().andIsNewEqualTo(true).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        List<Integer> goodsIds = goodsAgentService.getGoodsIds();
        if (goodsIds.size() > 0) {
            criteria.andIdIn(goodsIds);
        }
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 获取分类下的商品
     *
     * @param catList
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByCategory(List<Integer> catList, int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andCategoryIdIn(catList).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time  desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }


    /**
     * 获取分类下的商品
     *
     * @param catId
     * @param offset
     * @param limit
     * @return
     */
    public List<LitemallGoods> queryByCategory(Integer catId, int offset, int limit) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andCategoryIdEqualTo(catId).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        example.setOrderByClause("add_time desc");
        PageHelper.startPage(offset, limit);

        return goodsMapper.selectByExampleSelective(example, columns);
    }


    public List<LitemallGoods> querySelective(Integer catId, Integer brandId, String keywords, Boolean isHot, Boolean isNew, Integer offset, Integer limit, String sort, String order) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria1 = example.or();
        LitemallGoodsExample.Criteria criteria2 = example.or();

        if (!StringUtils.isEmpty(catId) && catId != 0) {
            criteria1.andCategoryIdEqualTo(catId);
            criteria2.andCategoryIdEqualTo(catId);
        }
        if (!StringUtils.isEmpty(brandId)) {
            criteria1.andBrandIdEqualTo(brandId);
            criteria2.andBrandIdEqualTo(brandId);
        }
        if (!StringUtils.isEmpty(isNew)) {
            criteria1.andIsNewEqualTo(isNew);
            criteria2.andIsNewEqualTo(isNew);
        }
        if (!StringUtils.isEmpty(isHot)) {
            criteria1.andIsHotEqualTo(isHot);
            criteria2.andIsHotEqualTo(isHot);
        }
        if (!StringUtils.isEmpty(keywords)) {
            criteria1.andKeywordsLike("%" + keywords + "%");
            criteria2.andNameLike("%" + keywords + "%");
        }
        criteria1.andIsOnSaleEqualTo(true);
        criteria2.andIsOnSaleEqualTo(true);
        criteria1.andDeletedEqualTo(false);
        criteria2.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }
        List<Integer> goodsIds = goodsAgentService.getGoodsIds();
        if (goodsIds.size() > 0) {
            criteria1.andIdIn(goodsIds);
            criteria2.andIdIn(goodsIds);
        }
        PageHelper.startPage(offset, limit);

        List<LitemallGoods> goodsList = goodsMapper.selectByExampleSelective(example, columns);
        return this.modifyGoods(goodsList);
    }

    public List<LitemallGoods> querySelective(Integer goodsId, String goodsSn, String name, Integer page, Integer size, String sort, String order) {
        List<Integer> goodsIds = null;
        if (goodsId != null) {
            goodsIds = Lists.newArrayList(goodsId);
        }
        return querySelective(goodsIds, goodsSn, name, page, size, sort, order);
    }

    public List<LitemallGoods> querySelective(List<Integer> goodsIds, String goodsSn, String name, Integer page, Integer size, String sort, String order) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria = example.createCriteria();

        if (goodsIds != null && goodsIds.size() > 0) {
            criteria.andIdIn(goodsIds);
        }
        if (!StringUtils.isEmpty(goodsSn)) {
            criteria.andGoodsSnEqualTo(goodsSn);
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return goodsMapper.selectByExampleWithBLOBs(example);
    }

    /**
     * 获取某个商品信息,包含完整信息
     *
     * @param id
     * @return
     */
    public LitemallGoods findById(Integer id) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdEqualTo(id).andDeletedEqualTo(false);
        return goodsMapper.selectOneByExampleWithBLOBs(example);
    }

    public List<LitemallGoods> findByIds(List<Integer> ids, LitemallGoods.Column... columns) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdIn(ids).andDeletedEqualTo(false);
        return goodsMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 获取某个商品信息，仅展示相关内容
     *
     * @param id
     * @return
     */
    public LitemallGoods findByIdVO(Integer id) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdEqualTo(id).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return goodsMapper.selectOneByExampleSelective(example, columns);
    }


    /**
     * 获取所有在售物品总数
     *
     * @return
     */
    public Integer queryOnSale() {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return (int) goodsMapper.countByExample(example);
    }

    public int updateById(LitemallGoods goods) {
        goods.setUpdateTime(LocalDateTime.now());
        return goodsMapper.updateByPrimaryKeySelective(goods);
    }

    public void deleteById(Integer id) {
        goodsMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallGoods goods) {
        goods.setAddTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.insertSelective(goods);
    }

    /**
     * 获取所有物品总数，包括在售的和下架的，但是不包括已删除的商品
     *
     * @return
     */
    public int count() {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andDeletedEqualTo(false);
        return (int) goodsMapper.countByExample(example);
    }

    public List<Integer> getCatIds(Integer brandId, String keywords, Boolean isHot, Boolean isNew) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        LitemallGoodsExample.Criteria criteria1 = example.or();
        LitemallGoodsExample.Criteria criteria2 = example.or();

        if (!StringUtils.isEmpty(brandId)) {
            criteria1.andBrandIdEqualTo(brandId);
            criteria2.andBrandIdEqualTo(brandId);
        }
        if (!StringUtils.isEmpty(isNew)) {
            criteria1.andIsNewEqualTo(isNew);
            criteria2.andIsNewEqualTo(isNew);
        }
        if (!StringUtils.isEmpty(isHot)) {
            criteria1.andIsHotEqualTo(isHot);
            criteria2.andIsHotEqualTo(isHot);
        }
        if (!StringUtils.isEmpty(keywords)) {
            criteria1.andKeywordsLike("%" + keywords + "%");
            criteria2.andNameLike("%" + keywords + "%");
        }
        criteria1.andIsOnSaleEqualTo(true);
        criteria2.andIsOnSaleEqualTo(true);
        criteria1.andDeletedEqualTo(false);
        criteria2.andDeletedEqualTo(false);

        List<Integer> goodsIds = goodsAgentService.getGoodsIds();
        if (goodsIds.size() > 0) {
            criteria1.andIdIn(goodsIds);
            criteria2.andIdIn(goodsIds);
        }

        List<LitemallGoods> goodsList = goodsMapper.selectByExampleSelective(example, Column.categoryId);
        goodsList = this.modifyGoods(goodsList);
        List<Integer> cats = new ArrayList<Integer>();
        for (LitemallGoods goods : goodsList) {
            cats.add(goods.getCategoryId());
        }
        return cats;
    }

    public boolean checkExistByName(String name) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andNameEqualTo(name).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return goodsMapper.countByExample(example) != 0;
    }

    public List<LitemallGoods> queryByIds(Integer[] ids) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdIn(Arrays.asList(ids)).andIsOnSaleEqualTo(true).andDeletedEqualTo(false);
        return queryByIds(ids, columns);
    }

    public List<LitemallGoods> queryByIds(Integer[] ids, LitemallGoods.Column... columns) {
        LitemallGoodsExample example = new LitemallGoodsExample();
        example.or().andIdIn(Arrays.asList(ids)).andDeletedEqualTo(false);
        return goodsMapper.selectByExampleSelective(example, columns);
    }

    public List<LitemallGoods> querySelective(LitemallGoods goods, LitemallGoods.Column... columns) {
        LitemallGoodsExample example = QueryUtil.constructExampleInstance(goods, LitemallGoodsExample.class);
        LitemallGoodsExample.Criteria criteria = example.getOredCriteria().get(0);
        criteria.andIsOnSaleEqualTo(true);

        // 如果没有按照id查，则指定id查询
        if (goods.getId() == null) {
            List<Integer> goodsIds = goodsAgentService.getGoodsIds();
            if (goodsIds.size() > 0) {
                criteria.andIdIn(goodsIds);
            }
        }

        List<Column> columnList;
        if (columns != null) {
            columnList = Lists.newArrayList(columns);
            if (!columnList.contains(Column.id)) {
                columnList.add(Column.id);
            }
        } else {
            columnList = Lists.newArrayListWithCapacity(1);
            columnList.add(Column.id);
        }
        return this.modifyGoods(goodsMapper.selectByExampleSelective(example, columnList.toArray(new Column[columnList.size()])));
    }

    /**
     * 过滤掉不可见的商品
     * @param goodsList
     * @return
     */
    private List<LitemallGoods> modifyGoods(List<LitemallGoods> goodsList) {
        log.info("Begin to modify goods attribute, include filter isShown, modify retailPrice");
        final Map<Integer, LitemallGoodsExtraInfo> extraInfoMap;
        if (goodsList.size() > 0) {
            List<Integer> goodsIdList = goodsList.stream().map(LitemallGoods::getId).collect(Collectors.toList());
            List<LitemallGoodsExtraInfo> extraInfoList = goodsExtraInfoService.queryByGoodsidsAndAdminId(goodsIdList, AgentHolder.getAgent().getId());
            extraInfoMap = extraInfoList.stream().collect(Collectors.toMap(LitemallGoodsExtraInfo::getGoodsId, Function.identity()));
        } else {
            extraInfoMap = Maps.newHashMap();
        }

        List<LitemallGoodsProductAgent> gpaList = gpaService.queryByAgentId(AgentHolder.getAgent().getId());
        Map<Integer, LitemallGoodsProductAgent> gpaMap = gpaList.stream().collect(Collectors.toMap(LitemallGoodsProductAgent::getGoodsId, Function.identity(), (a, b) -> {
            if (a.getPrice().compareTo(b.getPrice()) < 0) {
                return a;
            } else {
                return b;
            }
        }));
        LitemallGoodsExtraInfo defaultExtraInfo = new LitemallGoodsExtraInfo();
        defaultExtraInfo.setIsShow(true);

        List<LitemallGoods> collect = goodsList.stream().filter(goods -> extraInfoMap.getOrDefault(goods.getId(), defaultExtraInfo).getIsShow())
                .map(goods -> {
                    if (gpaMap.get(goods.getId()) != null) {
                        goods.setRetailPrice(gpaMap.get(goods.getId()).getPrice());
                    }
                    return goods;
                })
                .collect(Collectors.toList());
        log.info("End to to modify goods attribute");
        return collect;
    }
}
