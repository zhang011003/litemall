package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.db.dao.GoodsProductExtraInfoMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsProductExtraInfoMapper;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductExtraInfo;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductExtraInfoExample;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LitemallGoodsProductExtraInfoService {
    @Resource
    private LitemallGoodsProductExtraInfoMapper litemallGoodsProductExtraInfoMapper;
    private GoodsProductExtraInfoMapper goodsProductExtraInfoMapper;

    public List<LitemallGoodsProductExtraInfo> queryByGoodsidAndAdminId(Integer gid, Integer adminId) {
        LitemallGoodsProductExtraInfoExample example = new LitemallGoodsProductExtraInfoExample();
        example.or().andGoodsIdEqualTo(gid).andAdminIdEqualTo(adminId).andDeletedEqualTo(false);
        return litemallGoodsProductExtraInfoMapper.selectByExample(example);
    }

    public List<LitemallGoodsProductExtraInfo> queryByAdminId(Integer adminId) {
        LitemallGoodsProductExtraInfoExample example = new LitemallGoodsProductExtraInfoExample();
        example.or().andAdminIdEqualTo(adminId).andDeletedEqualTo(false);
        return litemallGoodsProductExtraInfoMapper.selectByExample(example);
    }

    public List<LitemallGoodsProductExtraInfo> queryByAdmin(LitemallGoodsProductExtraInfo gpa) {
        return litemallGoodsProductExtraInfoMapper.selectByExample(QueryUtil.constructExampleInstance(gpa, LitemallGoodsProductExtraInfoExample.class));
    }

    public LitemallGoodsProductExtraInfo queryByProductId(Integer productId, Integer adminId) {
        List<LitemallGoodsProductExtraInfo> extraInfos = queryByProductIds(Lists.newArrayList(productId), adminId);
        if (extraInfos.size() > 0) {
            return extraInfos.get(0);
        }
        return new LitemallGoodsProductExtraInfo();
    }

    public List<LitemallGoodsProductExtraInfo> queryByProductIds(List<Integer> productIds, Integer adminId, LitemallGoodsProductExtraInfo.Column... columns) {
        LitemallGoodsProductExtraInfo extraInfo = new LitemallGoodsProductExtraInfo();
        extraInfo.setAdminId(adminId);
        LitemallGoodsProductExtraInfoExample example = QueryUtil.constructExampleInstance(extraInfo, LitemallGoodsProductExtraInfoExample.class);
        LitemallGoodsProductExtraInfoExample.Criteria criteria = example.getOredCriteria().get(0);
        criteria.andProductIdIn(productIds);
        return litemallGoodsProductExtraInfoMapper.selectByExampleSelective(example,columns);
    }

    public int save(LitemallGoodsProductExtraInfo goodsProductExtraInfo) {
//        goodsProductExtraInfo.setAddTime(LocalDateTime.now());
//        goodsProductExtraInfo.setUpdateTime(LocalDateTime.now());
//        goodsProductExtraInfo.setDeleted(false);
//        return goodsProductExtraInfoMapper.insertSelective(goodsProductExtraInfo);

        // 先判断是否存在，如果存在则是多次派货的情况，此时只更新库存即可
        LitemallGoodsProductExtraInfo queryAgent = new LitemallGoodsProductExtraInfo();
        queryAgent.setGoodsId(goodsProductExtraInfo.getGoodsId());
        queryAgent.setProductId(goodsProductExtraInfo.getProductId());
        queryAgent.setAdminId(goodsProductExtraInfo.getAdminId());
        queryAgent.setBasePrice(goodsProductExtraInfo.getBasePrice());
        queryAgent.setParentAdminId(goodsProductExtraInfo.getParentAdminId());
        List<LitemallGoodsProductExtraInfo> gpas = queryByAdmin(queryAgent);
        if (gpas.size() > 0) {
            log.info("Already dispatched goods by this base price. " +
                            "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getProductId(), queryAgent.getAdminId(),
                    queryAgent.getBasePrice().doubleValue(), queryAgent.getParentAdminId());
            LitemallGoodsProductExtraInfo gpa = gpas.get(0);
            LitemallGoodsProductExtraInfo extraInfoInDB = new LitemallGoodsProductExtraInfo();
            extraInfoInDB.setId(gpa.getId());
            extraInfoInDB.setNumber(gpa.getNumber() + goodsProductExtraInfo.getNumber());
            extraInfoInDB.setUpdateTime(LocalDateTime.now());
            litemallGoodsProductExtraInfoMapper.updateByPrimaryKeySelective(extraInfoInDB);
        } else {
            log.info("Does not dispatch goods by this base price. " +
                            "goodsId:{}, goodsProductId:{}, agentId:{}, basePrice:{}, parentAgentId:{}",
                    queryAgent.getGoodsId(), queryAgent.getProductId(), queryAgent.getAdminId(),
                    queryAgent.getBasePrice().doubleValue(), queryAgent.getParentAdminId());
            goodsProductExtraInfo.setAddTime(LocalDateTime.now());
            goodsProductExtraInfo.setUpdateTime(LocalDateTime.now());
            litemallGoodsProductExtraInfoMapper.insertSelective(goodsProductExtraInfo);
        }
        return goodsProductExtraInfo.getId();
    }

    public void updateById(LitemallGoodsProductExtraInfo extraInfo) {
        extraInfo.setUpdateTime(LocalDateTime.now());
        litemallGoodsProductExtraInfoMapper.updateByPrimaryKeySelective(extraInfo);
    }

    public int updateByProductIdAndAdminId(LitemallGoodsProductExtraInfo productExtraInfo) {
        LitemallGoodsProductExtraInfo extraInfo = new LitemallGoodsProductExtraInfo();
        extraInfo.setProductId(productExtraInfo.getProductId());
        extraInfo.setAdminId(productExtraInfo.getAdminId());
        extraInfo.setUpdateTime(productExtraInfo.getUpdateTime());
        productExtraInfo.setUpdateTime(LocalDateTime.now());
        return litemallGoodsProductExtraInfoMapper.updateByExampleSelective(productExtraInfo,
                QueryUtil.constructExampleInstance(extraInfo, LitemallGoodsProductExtraInfoExample.class));
    }

    public int saveOrUpdate(LitemallGoodsProductExtraInfo goodsProductExtraInfo) {
        LitemallGoodsProductExtraInfo extraInfo = new LitemallGoodsProductExtraInfo();
        extraInfo.setProductId(goodsProductExtraInfo.getProductId());
        extraInfo.setAdminId(goodsProductExtraInfo.getAdminId());
        LitemallGoodsProductExtraInfoExample example = QueryUtil.constructExampleInstance(extraInfo, LitemallGoodsProductExtraInfoExample.class);
        LitemallGoodsProductExtraInfo extraInfoInDB = litemallGoodsProductExtraInfoMapper.selectOneByExampleSelective(example);
        if (extraInfoInDB.getId() != null) {
            return updateByProductIdAndAdminId(goodsProductExtraInfo);
        } else {
            return save(goodsProductExtraInfo);
        }
    }

    public List<LitemallGoodsProductExtraInfo> list(Integer currentUserId, Integer page, Integer size, String sort, String order) {
        LitemallGoodsProductExtraInfoExample example = new LitemallGoodsProductExtraInfoExample();
        LitemallGoodsProductExtraInfoExample.Criteria criteria = example.createCriteria();

        criteria.andAdminIdEqualTo(currentUserId);

        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return litemallGoodsProductExtraInfoMapper.selectByExample(example);
    }

    public int addStock(Integer id, Integer num){
        return goodsProductExtraInfoMapper.addStock(id, num);
    }

    public int reduceStock(Integer id, Integer num){
        return goodsProductExtraInfoMapper.reduceStock(id, num);
    }

}