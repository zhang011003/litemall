package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallAccountHistoryMapper;
import org.linlinjava.litemall.db.domain.LitemallAccount;
import org.linlinjava.litemall.db.domain.LitemallAccountHistory;
import org.linlinjava.litemall.db.domain.LitemallAccountHistoryExample;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAccountHistoryService {
    @Resource
    private LitemallAccountHistoryMapper accountHistoryMapper;

    @Resource
    private LitemallAccountService accountService;

//    public List<LitemallAccountHistory> findByAdminId(Integer adminId) {
//        LitemallAccount account = accountService.findByAdminIdSelective(adminId, LitemallAccount.Column.id);
//        return findByAccountIdSelective(account.getId());
//    }
//
//    public List<LitemallAccountHistory> findByAccountIdSelective(Integer accountId, LitemallAccountHistory.Column... columns) {
//        LitemallAccountHistory accountHistory = new LitemallAccountHistory();
//        accountHistory.setAccountId(accountId);
//        return findByAccountHistorySelective(accountHistory, columns);
//    }

    public List<LitemallAccountHistory> findByAccountHistorySelective(LitemallAccountHistory accountHistory, Integer page, Integer limit, String sort, String order, LitemallAccountHistory.Column... columns) {
        LitemallAccountHistoryExample example = QueryUtil.constructExampleInstance(accountHistory, LitemallAccountHistoryExample.class);

        // detail替换为like查询
        if (StringUtils.hasText(accountHistory.getDetail())) {
            List<LitemallAccountHistoryExample.Criterion> allCriteria = example.getOredCriteria().get(0).getAllCriteria();
            int index = -1;
            for (int i = 0; i < allCriteria.size(); i++) {
                LitemallAccountHistoryExample.Criterion criterion = allCriteria.get(i);
                if (criterion.getCondition().contains(LitemallAccountHistory.Column.detail.getValue())) {
                    index = i;
                    break;
                }
            }
            if (index > -1) {
                allCriteria.remove(index);
                example.getOredCriteria().get(0).andDetailLike("%" + accountHistory.getDetail() + "%");
            }
        }

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return accountHistoryMapper.selectByExampleSelective(example, columns);
    }

    public void insertHistories(List<LitemallAccountHistory> historyList) {
        insertHistories(historyList, 0);
    }

    /**
     * 多次增加历史时，时间一致导致账户管理获取到的数据顺序错乱，需要手工增加秒
     * @param historyList
     * @param secondIncr
     */
    public void insertHistories(List<LitemallAccountHistory> historyList, int secondIncr) {
        for (LitemallAccountHistory accountHistory : historyList) {
            accountHistory.setAddTime(LocalDateTime.now().plusSeconds(secondIncr));
            accountHistory.setUpdateTime(LocalDateTime.now().plusSeconds(secondIncr));
            accountHistory.setDeleted(false);
        }
        accountHistoryMapper.batchInsert(historyList);
    }

    public LitemallAccountHistory findLatestAccountHistory(Integer adminId, LitemallAccountHistory.Column... columns) {
        LitemallAccountHistory accountHistory = new LitemallAccountHistory();
        accountHistory.setAdminId(adminId);

        List<LitemallAccountHistory> histories = findByAccountHistorySelective(accountHistory, 1, 1, "add_time", "desc", columns);
        if (histories.size() > 0) {
            return histories.get(0);
        } else {
            return new LitemallAccountHistory();
        }

    }
}
