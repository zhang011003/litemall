package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.linlinjava.litemall.db.dao.LitemallCategoryMapper;
import org.linlinjava.litemall.db.domain.LitemallCategory;
import org.linlinjava.litemall.db.domain.LitemallCategoryExample;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LitemallCategoryService {
    @Resource
    private LitemallCategoryMapper categoryMapper;
    private LitemallCategory.Column[] CHANNEL = {LitemallCategory.Column.id, LitemallCategory.Column.name, LitemallCategory.Column.iconUrl};

    @Autowired
    private LitemallGoodsService goodsService;

    public List<LitemallCategory> queryL1WithoutRecommend(int offset, int limit) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andNameNotEqualTo("推荐").andDeletedEqualTo(false);
        PageHelper.startPage(offset, limit);
        return categoryMapper.selectByExample(example);
    }

    public List<LitemallCategory> queryL1(int offset, int limit) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        PageHelper.startPage(offset, limit);
        return categoryMapper.selectByExample(example);
    }

    public List<LitemallCategory> queryL1() {
        return queryL1(true);
    }

    /**
     * 是否包括没有商品的分类
     * @param includeEmptyGoods
     * @return
     */
    public List<LitemallCategory> queryL1(boolean includeEmptyGoods) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        LitemallCategoryExample.Criteria criteria = example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        if (!includeEmptyGoods) {
            List<LitemallGoods> litemallGoods = goodsService.querySelective(new LitemallGoods(), LitemallGoods.Column.id, LitemallGoods.Column.categoryId);
            Set<Integer> categoryIds = litemallGoods.stream().map(LitemallGoods::getCategoryId).collect(Collectors.toSet());
            LitemallCategoryExample l2Example = new LitemallCategoryExample();
            if (categoryIds.size() > 0) {
                l2Example.createCriteria().andIdIn(Lists.newArrayList(categoryIds));
                List<LitemallCategory> categories = categoryMapper.selectByExampleSelective(l2Example, LitemallCategory.Column.pid);
                Set<Integer> pids = categories.stream().map(LitemallCategory::getPid).collect(Collectors.toSet());
                if (pids.size() > 0) {
                    criteria.andIdIn(Lists.newArrayList(pids));
                } else {
                    return Lists.newArrayList();
                }
            } else {
                return Lists.newArrayList();
            }
        }
        return categoryMapper.selectByExample(example);
    }

    public List<LitemallCategory> queryByPid(Integer pid) {
        return queryByPid(pid, true);
    }

    public List<LitemallCategory> queryByPid(Integer pid, boolean includeEmptyGoods) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        LitemallCategoryExample.Criteria criteria = example.or().andPidEqualTo(pid).andDeletedEqualTo(false);
        if (!includeEmptyGoods) {
            List<LitemallGoods> litemallGoods = goodsService.querySelective(new LitemallGoods(), LitemallGoods.Column.categoryId);
            Set<Integer> categoryIds = litemallGoods.stream().map(LitemallGoods::getCategoryId).collect(Collectors.toSet());
            criteria.andIdIn(Lists.newArrayList(categoryIds));
        }
        return categoryMapper.selectByExample(example);
    }

    public List<LitemallCategory> queryL2ByIds(List<Integer> ids) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        example.or().andIdIn(ids).andLevelEqualTo("L2").andDeletedEqualTo(false);
        return categoryMapper.selectByExample(example);
    }

    public LitemallCategory findById(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    public List<LitemallCategory> querySelective(String id, String name, Integer page, Integer size, String sort, String order) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        LitemallCategoryExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return categoryMapper.selectByExample(example);
    }

    public int updateById(LitemallCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        return categoryMapper.updateByPrimaryKeySelective(category);
    }

    public void deleteById(Integer id) {
        categoryMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallCategory category) {
        category.setAddTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.insertSelective(category);
    }

    public List<LitemallCategory> queryChannel(boolean includeEmptyGoods) {
        LitemallCategoryExample example = new LitemallCategoryExample();
        LitemallCategoryExample.Criteria criteria = example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        if (!includeEmptyGoods) {
            List<LitemallGoods> litemallGoods = goodsService.querySelective(new LitemallGoods(), LitemallGoods.Column.categoryId);
            Set<Integer> categoryIds = litemallGoods.stream().map(LitemallGoods::getCategoryId).collect(Collectors.toSet());
            LitemallCategoryExample l2Example = new LitemallCategoryExample();
            l2Example.createCriteria().andIdIn(Lists.newArrayList(categoryIds));
            List<LitemallCategory> categories = categoryMapper.selectByExampleSelective(l2Example, LitemallCategory.Column.pid);
            Set<Integer> pids = categories.stream().map(LitemallCategory::getPid).collect(Collectors.toSet());
            criteria.andIdIn(Lists.newArrayList(pids));
        }
        return categoryMapper.selectByExampleSelective(example, CHANNEL);
    }
}
