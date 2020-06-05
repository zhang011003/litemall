package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.linlinjava.litemall.admin.dto.AdminAccountTransfer;
import org.linlinjava.litemall.admin.dto.Goods;
import org.linlinjava.litemall.admin.dto.GoodsAllinone;
import org.linlinjava.litemall.admin.vo.CatVo;
import org.linlinjava.litemall.admin.vo.GoodsProductVo;
import org.linlinjava.litemall.core.qcode.QCodeService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.*;
import org.linlinjava.litemall.db.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.GOODS_NAME_EXIST;

@Service
@Slf4j
public class AdminAccountService {

    @Autowired
    private LitemallAccountService accountService;
    @Autowired
    private LitemallAccountHistoryService accountHistoryService;
    @Autowired
    private LitemallAdminService adminService;

    public LitemallAccount findByAdminId() {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        return findByAdminId(litemallAdmin.getId());
    }

    public LitemallAccount findByAdminId(Integer adminId) {
        LitemallAccount account = accountService.findByAdminIdSelective(adminId, LitemallAccount.Column.balance);
        if (account.getBalance() == null) {
            account = new LitemallAccount();
            account.setAdminId(adminId);
            accountService.initAccount(account);
        }
        return account;
    }

    @Transactional
    public void transfer(AdminAccountTransfer transfer) {

        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        LitemallAccount currentAccount = findByAdminId(litemallAdmin.getId());

        LitemallAccount toAccount = findByAdminId(transfer.getTransferTo());
        LitemallAdmin toUser = adminService.findById(transfer.getTransferTo());

        // 增加转出历史记录
        LitemallAccountHistory currentUserAccountHistory = new LitemallAccountHistory();
        currentUserAccountHistory.setType((byte) 2);
        currentUserAccountHistory.setAdminId(litemallAdmin.getId());
        currentUserAccountHistory.setDetail(String.format("转账给%s", toUser.getUsername()));
        currentUserAccountHistory.setMoney(transfer.getMoney());

        currentUserAccountHistory.setBalance(currentAccount.getBalance().subtract(transfer.getMoney()));
        currentUserAccountHistory.setAddTime(LocalDateTime.now());
        currentUserAccountHistory.setUpdateTime(LocalDateTime.now());
        currentUserAccountHistory.setDeleted(false);

        // 增加转入历史记录
        LitemallAccountHistory toUserAccountHistory = new LitemallAccountHistory();
        toUserAccountHistory.setType((byte) 1);
        toUserAccountHistory.setAdminId(transfer.getTransferTo());
        toUserAccountHistory.setMoney(transfer.getMoney());

        toUserAccountHistory.setBalance(toAccount.getBalance().add(transfer.getMoney()));
        toUserAccountHistory.setDetail(String.format("收到%s的转账", litemallAdmin.getUsername()));
        toUserAccountHistory.setAddTime(LocalDateTime.now());
        toUserAccountHistory.setUpdateTime(LocalDateTime.now());
        toUserAccountHistory.setDeleted(false);

        accountHistoryService.insertHistories(Lists.newArrayList(currentUserAccountHistory, toUserAccountHistory));

        // 修改账户余额
        accountService.updateAccount(litemallAdmin.getId(), transfer.getMoney(), false);
        accountService.updateAccount(transfer.getTransferTo(), transfer.getMoney(), true);
    }
}
