package org.linlinjava.litemall.db.service;

import com.google.common.collect.Lists;
import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.dao.GoodsProductMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsProductMapper;
import org.linlinjava.litemall.db.domain.LitemallGoodsProduct;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductExample;
import org.linlinjava.litemall.db.util.AgentHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LitemallGoodsProductService {
    @Resource
    private LitemallGoodsProductMapper litemallGoodsProductMapper;
    @Resource
    private GoodsProductMapper goodsProductMapper;
    @Resource
    private GoodsAgentService goodsAgentService;
    @Resource
    private LitemallGoodsProductAgentService goodsProductAgentService;

    public List<LitemallGoodsProduct> queryByGid(Integer gid) {
        LitemallGoodsProductExample example = new LitemallGoodsProductExample();

        List<Integer> goodsIds = goodsAgentService.getGoodsIds();
        Map<Integer, List<LitemallGoodsProductAgent>> goodsProductAgentMap = null;
        if (goodsIds.size() > 0 && goodsIds.contains(gid)) {
            List<LitemallGoodsProductAgent> goodsProductAgents = goodsProductAgentService.queryByGidAndAgentId(gid, AgentHolder.getAgent().getId());
            goodsProductAgentMap = goodsProductAgents.stream().collect(Collectors.toMap(LitemallGoodsProductAgent::getGoodsProductId, agent -> Lists.newArrayList(agent), (a, b) -> {
                a.addAll(b);
                return a;
            }));
            example.or().andIdIn(Lists.newArrayList(goodsProductAgentMap.keySet()));
        } else {
            example.or().andGoodsIdEqualTo(gid).andDeletedEqualTo(false);
        }
        List<LitemallGoodsProduct> goodsProducts = litemallGoodsProductMapper.selectByExample(example);
        if (goodsProductAgentMap != null) {

            // 重新设置库存和价格
            for (LitemallGoodsProduct goodsProduct : goodsProducts) {
                List<LitemallGoodsProductAgent> goodsProductAgents = goodsProductAgentMap.get(goodsProduct.getId());
                Integer number = goodsProductAgents.stream().map(LitemallGoodsProductAgent::getNumber).reduce(0, Integer::sum);
                goodsProduct.setNumber(number);
                goodsProduct.setPrice(goodsProductAgents.get(0).getPrice());
            }
        }
        return goodsProducts;
    }

    public List<LitemallGoodsProduct> queryByGids(List<Integer> gids) {
        LitemallGoodsProductExample example = new LitemallGoodsProductExample();
        example.or().andGoodsIdIn(gids).andDeletedEqualTo(false);
        return litemallGoodsProductMapper.selectByExample(example);
    }

    public LitemallGoodsProduct findById(Integer id) {
        return litemallGoodsProductMapper.selectByPrimaryKey(id);
    }
    public List<LitemallGoodsProduct> findByIds(List<Integer> ids, LitemallGoodsProduct.Column... columns) {
        LitemallGoodsProductExample example = new LitemallGoodsProductExample();
        example.or().andIdIn(ids);
        return litemallGoodsProductMapper.selectByExampleSelective(example, columns);
    }

    public void deleteById(Integer id) {
        litemallGoodsProductMapper.logicalDeleteByPrimaryKey(id);
    }

    public void add(LitemallGoodsProduct goodsProduct) {
        goodsProduct.setAddTime(LocalDateTime.now());
        goodsProduct.setUpdateTime(LocalDateTime.now());
        litemallGoodsProductMapper.insertSelective(goodsProduct);
    }

    public int count() {
        LitemallGoodsProductExample example = new LitemallGoodsProductExample();
        example.or().andDeletedEqualTo(false);
        return (int) litemallGoodsProductMapper.countByExample(example);
    }

    public void deleteByGid(Integer gid) {
        LitemallGoodsProductExample example = new LitemallGoodsProductExample();
        example.or().andGoodsIdEqualTo(gid);
        litemallGoodsProductMapper.logicalDeleteByExample(example);
    }

    public int addStock(Integer id, Short num){
        return goodsProductMapper.addStock(id, num);
    }

    public int reduceStock(Integer id, Short num){
        return reduceStock(id, num.intValue());
    }

    public int reduceStock(Integer id, Integer num){
        return goodsProductMapper.reduceStock(id, num);
    }

    public void updateById(LitemallGoodsProduct product) {
        product.setUpdateTime(LocalDateTime.now());
        litemallGoodsProductMapper.updateByPrimaryKeySelective(product);
    }

}