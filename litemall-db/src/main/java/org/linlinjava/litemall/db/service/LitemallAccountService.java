package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.AccountMapper;
import org.linlinjava.litemall.db.dao.LitemallAccountMapper;
import org.linlinjava.litemall.db.domain.LitemallAccount;
import org.linlinjava.litemall.db.domain.LitemallAccountExample;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallAccountService {
    @Resource
    private LitemallAccountMapper litemallAccountMapper;
    @Resource
    private AccountMapper accountMapper;

    public LitemallAccount findByAdminId(Integer currentUserId) {
        return findByAdminIdSelective(currentUserId);
    }

    /**
     * 创建用户没有账户的情况，需要初始化一个账户
     * @param account
     */
    public void initAccount(LitemallAccount account) {
        if (account.getAdminId() == null) {
            throw new IllegalArgumentException("admin id cannot be null");
        }
        LitemallAccountExample example = QueryUtil.constructExampleInstance(account, LitemallAccountExample.class);
        if (litemallAccountMapper.countByExample(example) > 0) {
            return;
        }
        account.setBalance(BigDecimal.ZERO);
        account.setAddTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());
        account.setDeleted(Boolean.FALSE);
        litemallAccountMapper.insert(account);
    }
    public LitemallAccount findByAdminIdSelective(Integer adminId, LitemallAccount.Column... columns) {
        LitemallAccount account = new LitemallAccount();
        account.setAdminId(adminId);
        LitemallAccountExample example = QueryUtil.constructExampleInstance(account, LitemallAccountExample.class);
        List<LitemallAccount> accounts = litemallAccountMapper.selectByExampleSelective(example, columns);
        if (accounts.size() > 0) {
            return accounts.get(0);
        } else {
            return new LitemallAccount();
        }
    }

    public int updateAccount(Integer adminId, BigDecimal money, boolean isAdd) {
        return accountMapper.updateAccount(adminId, money, isAdd);
    }
}
