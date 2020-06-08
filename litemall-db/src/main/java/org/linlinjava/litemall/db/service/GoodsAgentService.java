package org.linlinjava.litemall.db.service;

import com.google.common.collect.Lists;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.util.AgentHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoodsAgentService {
    @Resource
    private LitemallGoodsProductAgentService gpaService;

    /**
     * 如果为分销场景，则返回代理商可以销售的商品，否则返回空list
     * @return
     */
    public List<Integer> getGoodsIds() {
        List<Integer> goodsIdList = Lists.newArrayList();
        LitemallAdmin agent = AgentHolder.getAgent();
        if (agent != null) {
            List<LitemallGoodsProductAgent> gpa = gpaService.queryByAgentId(agent.getId());
            Set<Integer> goodsIds = gpa.stream().map(LitemallGoodsProductAgent::getGoodsId).collect(Collectors.toSet());
            goodsIdList.addAll(goodsIds);
        }
        return goodsIdList;
    }
}
