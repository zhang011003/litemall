package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.db.dao.GoodsProductAgentMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsProductAgentMapper;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgentExample;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LitemallGoodsProductAgentService {
    @Resource
    private LitemallGoodsProductAgentMapper litemallGoodsProductAgentMapper;
    @Resource
    private GoodsProductAgentMapper goodsProductAgentMapper;

    public List<LitemallGoodsProductAgent> queryByGidAndAgentId(Integer gid, Integer agentId) {
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        example.or().andGoodsIdEqualTo(gid).andAgentIdEqualTo(agentId).andDeletedEqualTo(false);
        return litemallGoodsProductAgentMapper.selectByExample(example);
    }

    /**
     * 根据货品id和agentId获取货品信息，有可能同一个货品以不同基础价格多次派货的情况，所以返回的是list
     * @param gpid
     * @param agentId
     * @param columns
     * @return
     */
    public List<LitemallGoodsProductAgent> queryByGPidAndAgentId(Integer gpid, Integer agentId, LitemallGoodsProductAgent.Column... columns) {
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        example.or().andGoodsProductIdEqualTo(gpid).andAgentIdEqualTo(agentId).andDeletedEqualTo(false);
        return litemallGoodsProductAgentMapper.selectByExampleSelective(example, columns);
    }

    /**
     * 可能查出相同productId，相同adminId，但不同成本价格的数据
     * @param productIdList
     * @param adminId
     * @param columns
     * @return
     */
    public List<LitemallGoodsProductAgent> queryByProductIds(List<Integer> productIdList, Integer adminId, LitemallGoodsProductAgent.Column... columns) {
        if (productIdList.size() <= 0) {
            return Lists.newArrayList();
        }
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        example.or().andGoodsProductIdIn(productIdList).andAgentIdEqualTo(adminId).andDeletedEqualTo(false);
        return litemallGoodsProductAgentMapper.selectByExampleSelective(example, columns);
    }

    public List<LitemallGoodsProductAgent> queryByAgentId(Integer agentId) {
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        example.or().andAgentIdEqualTo(agentId).andDeletedEqualTo(false);
        return litemallGoodsProductAgentMapper.selectByExample(example);
    }

    public List<LitemallGoodsProductAgent> queryByAgent(LitemallGoodsProductAgent gpa) {
        return litemallGoodsProductAgentMapper.selectByExample(QueryUtil.constructExampleInstance(gpa, LitemallGoodsProductAgentExample.class));
    }

    public long countByAgent(LitemallGoodsProductAgent gpa) {
        return litemallGoodsProductAgentMapper.countByExample(QueryUtil.constructExampleInstance(gpa, LitemallGoodsProductAgentExample.class));
    }

    public LitemallGoodsProductAgent findById(Integer id) {
        return litemallGoodsProductAgentMapper.selectByPrimaryKey(id);
    }

    public void deleteById(Integer id) {
        litemallGoodsProductAgentMapper.logicalDeleteByPrimaryKey(id);
    }

    @Transactional
    public Integer add(LitemallGoodsProductAgent agent) {
        // 先判断是否存在，如果存在则是多次派货的情况，此时只更新库存即可
        LitemallGoodsProductAgent queryAgent = new LitemallGoodsProductAgent();
        queryAgent.setGoodsId(agent.getGoodsId());
        queryAgent.setGoodsProductId(agent.getGoodsProductId());
        queryAgent.setAgentId(agent.getAgentId());
        queryAgent.setBasePrice(agent.getBasePrice());
        queryAgent.setParentAgentId(agent.getParentAgentId());
        List<LitemallGoodsProductAgent> gpas = queryByAgent(queryAgent);
        if (gpas.size() > 0) {
            log.info("已经存在该记录。 " +
                    "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getGoodsProductId(), queryAgent.getAgentId(),
                    queryAgent.getBasePrice() != null?queryAgent.getBasePrice().doubleValue():null, queryAgent.getParentAgentId());
            LitemallGoodsProductAgent gpa = gpas.get(0);
            LitemallGoodsProductAgent agentInDb = new LitemallGoodsProductAgent();
            agentInDb.setId(gpa.getId());

            // 如果是管理员,则修改库存，否则库存增加
            if (gpas.size() == 1 && gpas.get(0).getParentAgentId() == null) {
                agentInDb.setNumber(agent.getNumber());
            } else {
                if (agent.getNumber() != null) {
                    agentInDb.setNumber(gpa.getNumber() + agent.getNumber());
                }
            }
            agentInDb.setUpdateTime(LocalDateTime.now());

            LitemallGoodsProductAgent tmpAgent = new LitemallGoodsProductAgent();
            tmpAgent.setId(gpa.getId());
            tmpAgent.setUpdateTime(gpa.getUpdateTime());
            litemallGoodsProductAgentMapper.updateByExampleSelective(agentInDb,
                    QueryUtil.constructExampleInstance(tmpAgent, LitemallGoodsProductAgentExample.class));

//            if (gpas.size() > 1) {
//                // 更新agentId和productId列值相同的价格和派货价格
//                agentInDb = new LitemallGoodsProductAgent();
//                if (agent.getPrice() != null) {
//                    agentInDb.setPrice(agent.getPrice());
//                }
//                agentInDb.setDispatchPrice(agent.getDispatchPrice());
//                tmpAgent = new LitemallGoodsProductAgent();
//                tmpAgent.setGoodsProductId(gpa.getGoodsProductId());
//                tmpAgent.setAgentId(agent.getAgentId());
//                litemallGoodsProductAgentMapper.updateByExampleSelective(agentInDb,
//                        QueryUtil.constructExampleInstance(tmpAgent, LitemallGoodsProductAgentExample.class));
//            }
        } else {
            log.info("还没有存在相应记录. " +
                    "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getGoodsProductId(), queryAgent.getAgentId(),
                    queryAgent.getBasePrice() != null?queryAgent.getBasePrice().doubleValue():null, queryAgent.getParentAgentId());

            // 如果之前已经派给代理商货，则新增加的派货记录需要修改派货价格和售价为最新的记录
            List<LitemallGoodsProductAgent> list = this.queryByGPidAndAgentId(agent.getGoodsProductId(), agent.getAgentId(),
                    LitemallGoodsProductAgent.Column.dispatchPrice, LitemallGoodsProductAgent.Column.price);
            if (list.size() > 0) {
                agent.setDispatchPrice(list.get(0).getDispatchPrice());
                agent.setPrice(list.get(0).getPrice());
            }
            agent.setAddTime(LocalDateTime.now());
            agent.setUpdateTime(LocalDateTime.now());
            litemallGoodsProductAgentMapper.insertSelective(agent);
        }
        return agent.getId();
    }

    public void updatePrice(LitemallGoodsProductAgent agent) {
        LitemallGoodsProductAgent queryAgent = new LitemallGoodsProductAgent();
        queryAgent.setGoodsId(agent.getGoodsId());
        queryAgent.setGoodsProductId(agent.getGoodsProductId());
        queryAgent.setAgentId(agent.getAgentId());
        queryAgent.setBasePrice(agent.getBasePrice());
        queryAgent.setParentAgentId(agent.getParentAgentId());

        litemallGoodsProductAgentMapper.updateByExampleSelective(agent,
                QueryUtil.constructExampleInstance(queryAgent, LitemallGoodsProductAgentExample.class));
    }

    public void updateById(LitemallGoodsProductAgent agent) {
        agent.setUpdateTime(LocalDateTime.now());
        litemallGoodsProductAgentMapper.updateByPrimaryKeySelective(agent);
    }

    public List<LitemallGoodsProductAgent> list(Integer currentUserId, Integer page, Integer size, String sort, String order) {
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        LitemallGoodsProductAgentExample.Criteria criteria = example.createCriteria();

        criteria.andAgentIdEqualTo(currentUserId);

        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return litemallGoodsProductAgentMapper.selectByExample(example);
    }

    public int addStock(Integer id, Integer num){
        return goodsProductAgentMapper.addStock(id, num);
    }

    public int reduceStock(Integer id, Integer num){
        return goodsProductAgentMapper.reduceStock(id, num);
    }
}