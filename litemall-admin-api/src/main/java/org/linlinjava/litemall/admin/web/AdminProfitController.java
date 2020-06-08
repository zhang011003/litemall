package org.linlinjava.litemall.admin.web;

import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.admin.dto.AdminAccountTransfer;
import org.linlinjava.litemall.admin.dto.AdminProfitCashOut;
import org.linlinjava.litemall.admin.service.AdminAccountService;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/profit")
@Validated
public class AdminProfitController {

    @Autowired
    private AdminAccountService accountService;

    @GetMapping
    public Object list() {
        return ResponseUtil.ok(accountService.findByAdminId(AccountUtil.AccountType.PROFIT));
    }

    @PostMapping("cashout")
    public Object cashOut(@AdminLoginUser LitemallAdmin admin, @RequestBody AdminProfitCashOut cashOut) {
        //TODO: 提现功能
        cashOut.setAdmin(admin);
        return accountService.cashOut(cashOut);
    }
}
