package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.linlinjava.litemall.db.dao.GoodsProductExtraInfoMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsExtraInfoMapper;
import org.linlinjava.litemall.db.dao.LitemallGoodsProductExtraInfoMapper;
import org.linlinjava.litemall.db.domain.LitemallGoodsExtraInfo;
import org.linlinjava.litemall.db.domain.LitemallGoodsExtraInfoExample;
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
public class LitemallGoodsExtraInfoService {
    @Resource
    private LitemallGoodsExtraInfoMapper litemallGoodsExtraInfoMapper;

    public List<LitemallGoodsExtraInfo> queryByGoodsidsAndAdminId(List<Integer> goodsIdList, Integer adminId, LitemallGoodsExtraInfo.Column... columns) {
        if (goodsIdList.size() <= 0) {
            return Lists.newArrayList();
        }
        LitemallGoodsExtraInfoExample example = new LitemallGoodsExtraInfoExample();
        example.or().andGoodsIdIn(goodsIdList).andAdminIdEqualTo(adminId).andDeletedEqualTo(false);
        return litemallGoodsExtraInfoMapper.selectByExampleSelective(example, columns);
    }

    public void saveOrUpdateGoodsExtraInfo(LitemallGoodsExtraInfo extraInfo) {
        if (extraInfo.getId() != null) {
            extraInfo.setUpdateTime(LocalDateTime.now());
            litemallGoodsExtraInfoMapper.updateByPrimaryKeySelective(extraInfo);
        } else {
            if (extraInfo.getAdminId() == null || extraInfo.getGoodsId() == null) {
                throw new IllegalArgumentException("adminid or goodsid is null");
            }
            LitemallGoodsExtraInfo tmpExtraInfo = new LitemallGoodsExtraInfo();
            tmpExtraInfo.setAdminId(extraInfo.getAdminId());
            tmpExtraInfo.setGoodsId(extraInfo.getGoodsId());
            List<LitemallGoodsExtraInfo> extraInfoList = litemallGoodsExtraInfoMapper.selectByExampleSelective(
                    QueryUtil.constructExampleInstance(tmpExtraInfo, LitemallGoodsExtraInfoExample.class),
                    LitemallGoodsExtraInfo.Column.id);
            if (extraInfoList.size() > 0) {
                extraInfo.setId(extraInfoList.get(0).getId());
                extraInfo.setUpdateTime(LocalDateTime.now());
                litemallGoodsExtraInfoMapper.updateByPrimaryKeySelective(extraInfo);
            } else {
                extraInfo.setAddTime(LocalDateTime.now());
                extraInfo.setUpdateTime(LocalDateTime.now());
                extraInfo.setDeleted(false);
                litemallGoodsExtraInfoMapper.insertSelective(extraInfo);
            }
        }
    }
}