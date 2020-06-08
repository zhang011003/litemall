package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
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

    public LitemallGoodsProductAgent queryByGPid(Integer gpid) {
        LitemallGoodsProductAgentExample example = new LitemallGoodsProductAgentExample();
        example.or().andGoodsProductIdEqualTo(gpid).andDeletedEqualTo(false);
        return litemallGoodsProductAgentMapper.selectOneByExample(example);
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
            log.info("Already dispatched goods by this base price. " +
                    "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getGoodsProductId(), queryAgent.getAgentId(),
                    queryAgent.getBasePrice().doubleValue(), queryAgent.getParentAgentId());
            LitemallGoodsProductAgent gpa = gpas.get(0);
            LitemallGoodsProductAgent agentInDb = new LitemallGoodsProductAgent();
            agentInDb.setId(gpa.getId());
            agentInDb.setNumber(gpa.getNumber() + agent.getNumber());
            agentInDb.setUpdateTime(LocalDateTime.now());
            litemallGoodsProductAgentMapper.updateByPrimaryKeySelective(agentInDb);
        } else {
            log.info("Does not dispatch goods by this base price. " +
                    "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getGoodsProductId(), queryAgent.getAgentId(),
                    queryAgent.getBasePrice().doubleValue(), queryAgent.getParentAgentId());
            agent.setAddTime(LocalDateTime.now());
            agent.setUpdateTime(LocalDateTime.now());
            litemallGoodsProductAgentMapper.insertSelective(agent);
        }
        return agent.getId();
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