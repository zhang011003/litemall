package org.linlinjava.litemall.db.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.db.dao.GoodsProductMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsProductMapper;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallGoodsProduct;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductExample;
import org.linlinjava.litemall.db.util.AgentHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.management.resources.agent;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        if (ids.size() <= 0) {
            return Lists.newArrayList();
        }
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

    public int addStock(Integer id, Short num, Integer adminId){
        List<LitemallGoodsProductAgent> gpaList = this.goodsProductAgentService.queryByAgentId(adminId);
        if (gpaList.size() > 0) {
            return this.goodsProductAgentService.addStock(gpaList.get(0).getId(), num.intValue());
        } else {
            return goodsProductMapper.addStock(id, num);
        }
    }
    public int addStock(Integer id, Short num, LitemallAdmin admin){
        return addStock(id, num, admin.getId());
    }

    public int reduceStock(Integer id, Short num, LitemallAdmin admin){
        return reduceStock(id, num.intValue(), admin);
    }

    @Transactional
    public int reduceStock(Integer id, Integer num, LitemallAdmin admin){
        List<LitemallGoodsProductAgent> gpaList = this.goodsProductAgentService.queryByGPidAndAgentId(id, admin.getId());
        if (gpaList.size() > 0) {
            // 多次派货，则一个一个将库存减少，如果最后需要扣减的数量大于0，则说明需要扣减的库存大于实际库存
            int updateNum = 0;
            int totalNum = num;
            for (LitemallGoodsProductAgent gpa : gpaList) {
                if (gpa.getNumber() <= 0) {
                    continue;
                }
                if (totalNum > gpa.getNumber()) {
                    updateNum += this.goodsProductAgentService.reduceStock(gpa.getId(), gpa.getNumber());
                } else {
                    updateNum += this.goodsProductAgentService.reduceStock(gpa.getId(), totalNum);
                }
                totalNum = totalNum - gpa.getNumber();
                if (totalNum <= 0) {
                    break;
                }
            }
            if (totalNum > 0) {
                log.error("商品货品库存不足, 还剩{}无法扣减, productId:{}, num:{}, admin:{}",
                        totalNum, id, num, admin.getId());
                throw new RuntimeException("商品货品库存不足");
            } else {
                return updateNum;
            }
        } else {
            return goodsProductMapper.reduceStock(id, num);
        }
    }

    public void updateById(LitemallGoodsProduct product) {
        product.setUpdateTime(LocalDateTime.now());
        litemallGoodsProductMapper.updateByPrimaryKeySelective(product);
    }

}