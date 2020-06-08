package org.linlinjava.litemall.db.service;

import org.linlinjava.litemall.db.dao.AccountMapper;
import org.linlinjava.litemall.db.dao.LitemallAccountMapper;
import org.linlinjava.litemall.db.domain.LitemallAccount;
import org.linlinjava.litemall.db.domain.LitemallAccountExample;
import org.linlinjava.litemall.db.util.AccountUtil;
import org.linlinjava.litemall.db.util.QueryUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public LitemallAccount findByAdminId(Integer currentUserId, AccountUtil.AccountType accountType) {
        return findByAdminIdAccountSelective(currentUserId, accountType);
    }

    /**
     * 创建用户没有账户的情况，需要初始化一个账户
     * @param account
     */
    private void initAccount(LitemallAccount account) {
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

    public LitemallAccount findByAdminIdAccountSelective(Integer adminId, AccountUtil.AccountType accountType, LitemallAccount.Column... columns) {
        LitemallAccount account = new LitemallAccount();
        account.setAdminId(adminId);
        account.setAccountType(accountType.getAccountType());
        LitemallAccountExample example = QueryUtil.constructExampleInstance(account, LitemallAccountExample.class);
        List<LitemallAccount> accounts = litemallAccountMapper.selectByExampleSelective(example, columns);
        if (accounts.size() > 0) {
            return accounts.get(0);
        } else {
            account = new LitemallAccount();
            account.setAdminId(adminId);
            account.setAccountType(accountType.getAccountType());
            initAccount(account);
            return account;
        }
    }

    @Transactional
    public int updateAccount(Integer adminId, AccountUtil.AccountType accountType, BigDecimal money, boolean isAdd) {
        LocalDateTime updateTime = litemallAccountMapper.selectByPrimaryKeySelective(adminId, accountType.getAccountType(), LitemallAccount.Column.updateTime).getUpdateTime();
        return accountMapper.updateAccountWithOptimisticLocker(adminId, money, isAdd, updateTime);
    }
}
