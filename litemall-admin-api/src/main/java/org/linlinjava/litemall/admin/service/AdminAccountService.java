package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.dto.AdminAccountTransfer;
import org.linlinjava.litemall.admin.dto.AdminProfitCashOut;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.linlinjava.litemall.db.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AdminAccountService {

    @Autowired
    private LitemallAccountService accountService;
    @Autowired
    private LitemallAccountHistoryService accountHistoryService;
    @Autowired
    private LitemallAdminService adminService;

    public LitemallAccount findByAdminId(AccountUtil.AccountType accountType) {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        return findByAdminId(litemallAdmin.getId(), accountType);
    }

    public LitemallAccount findByAdminId(Integer adminId, AccountUtil.AccountType accountType) {
        return accountService.findByAdminIdAccountSelective(adminId, accountType, LitemallAccount.Column.balance);
    }

    @Transactional
    public void transfer(AdminAccountTransfer transfer) {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        LitemallAccount currentAccount = findByAdminId(litemallAdmin.getId(), AccountUtil.AccountType.ACCOUNT);

        LitemallAccount toAccount = findByAdminId(transfer.getTransferTo(), AccountUtil.AccountType.ACCOUNT);
        LitemallAdmin toUser = adminService.findById(transfer.getTransferTo());

        // 增加转出历史记录
        LitemallAccountHistory currentUserAccountHistory = new LitemallAccountHistory();
        currentUserAccountHistory.setType(AccountUtil.Type.OUTGOINGS.getType());
        currentUserAccountHistory.setAdminId(litemallAdmin.getId());
        currentUserAccountHistory.setAccountType(AccountUtil.AccountType.ACCOUNT.getAccountType());
        currentUserAccountHistory.setDetail(String.format("转账给%s", toUser.getUsername()));
        currentUserAccountHistory.setMoney(transfer.getMoney());

        currentUserAccountHistory.setBalance(currentAccount.getBalance().subtract(transfer.getMoney()));
        currentUserAccountHistory.setAddTime(LocalDateTime.now());
        currentUserAccountHistory.setUpdateTime(LocalDateTime.now());
        currentUserAccountHistory.setDeleted(false);

        // 增加转入历史记录
        LitemallAccountHistory toUserAccountHistory = new LitemallAccountHistory();
        toUserAccountHistory.setType(AccountUtil.Type.INCOME.getType());
        toUserAccountHistory.setAdminId(transfer.getTransferTo());
        toUserAccountHistory.setAccountType(AccountUtil.AccountType.ACCOUNT.getAccountType());
        toUserAccountHistory.setMoney(transfer.getMoney());

        toUserAccountHistory.setBalance(toAccount.getBalance().add(transfer.getMoney()));
        toUserAccountHistory.setDetail(String.format("收到%s的转账", litemallAdmin.getUsername()));
        toUserAccountHistory.setAddTime(LocalDateTime.now());
        toUserAccountHistory.setUpdateTime(LocalDateTime.now());
        toUserAccountHistory.setDeleted(false);

        accountHistoryService.insertHistories(Lists.newArrayList(currentUserAccountHistory, toUserAccountHistory));

        // 修改账户余额
        accountService.updateAccount(litemallAdmin.getId(), AccountUtil.AccountType.ACCOUNT, transfer.getMoney(), false);
        accountService.updateAccount(transfer.getTransferTo(), AccountUtil.AccountType.ACCOUNT, transfer.getMoney(), true);
    }

    /**
     * 提现
     * @param cashOut
     * @return
     */
    @Transactional
    public Object cashOut(AdminProfitCashOut cashOut) {

        LitemallAdmin litemallAdmin = cashOut.getAdmin();
        LitemallAccount currentAccount = findByAdminId(litemallAdmin.getId(), AccountUtil.AccountType.PROFIT);

        if (cashOut.getMoney().compareTo(currentAccount.getBalance()) > 0) {
            return ResponseUtil.fail(1, "提现金额大于利润账户余额");
        }
        LitemallAccountHistory latestAccountHistory = accountHistoryService.findLatestAccountHistory(
                litemallAdmin.getId(),
                AccountUtil.AccountType.PROFIT,
                AccountUtil.AccountStatus.CASHOUTING,
                LitemallAccountHistory.Column.balance,
                LitemallAccountHistory.Column.addTime);
        if (latestAccountHistory.getBalance() != null) {
            return ResponseUtil.fail(2, "请等待上一次提现完成");
        }
        // 增加转出历史记录
        LitemallAccountHistory currentUserAccountHistory = new LitemallAccountHistory();
        currentUserAccountHistory.setType(AccountUtil.Type.OUTGOINGS.getType());
        currentUserAccountHistory.setAdminId(litemallAdmin.getId());
        currentUserAccountHistory.setAccountType(AccountUtil.AccountType.PROFIT.getAccountType());
        currentUserAccountHistory.setDetail(String.format("%s提现", litemallAdmin.getUsername()));
        currentUserAccountHistory.setMoney(cashOut.getMoney());

        currentUserAccountHistory.setBalance(currentAccount.getBalance());
        currentUserAccountHistory.setAddTime(LocalDateTime.now());
        currentUserAccountHistory.setUpdateTime(LocalDateTime.now());
        currentUserAccountHistory.setDeleted(false);
        currentUserAccountHistory.setAccountStatus(AccountUtil.AccountStatus.CASHOUTING.getAccountStatus());
        accountHistoryService.insertHistories(Lists.newArrayList(currentUserAccountHistory));

        // 修改账户余额
//        accountService.updateAccount(litemallAdmin.getId(), AccountUtil.AccountType.PROFIT, cashOut.getMoney(), false);

        return ResponseUtil.ok();
    }
}
