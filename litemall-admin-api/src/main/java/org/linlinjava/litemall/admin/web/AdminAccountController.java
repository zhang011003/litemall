package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.admin.dto.AdminAccountTransfer;
import org.linlinjava.litemall.admin.service.AdminAccountService;
import org.linlinjava.litemall.core.util.JacksonUtil;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallAccount;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallNotice;
import org.linlinjava.litemall.db.domain.LitemallNoticeAdmin;
import org.linlinjava.litemall.db.service.LitemallAccountService;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallNoticeAdminService;
import org.linlinjava.litemall.db.service.LitemallNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.NOTICE_UPDATE_NOT_ALLOWED;

@RestController
@RequestMapping("/admin/account")
@Validated
public class AdminAccountController {

    @Autowired
    private AdminAccountService accountService;

    @GetMapping
    public Object list() {
        return ResponseUtil.ok(accountService.findByAdminId());
    }

    @PostMapping("transfer")
    public Object transfer(@RequestBody AdminAccountTransfer transfer) {
        BigDecimal balance = accountService.findByAdminId().getBalance();
        if (balance.compareTo(transfer.getMoney()) < 0) {
            return ResponseUtil.badArgumentValue();
        }
        accountService.transfer(transfer);
        return ResponseUtil.ok();
    }
}
