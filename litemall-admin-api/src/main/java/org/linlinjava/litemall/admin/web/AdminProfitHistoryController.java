package org.linlinjava.litemall.admin.web;

import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallAccountHistory;
import org.linlinjava.litemall.db.service.LitemallAccountHistoryService;
import org.linlinjava.litemall.db.service.LitemallAccountService;
import org.linlinjava.litemall.db.util.AccountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/profithistory")
@Validated
public class AdminProfitHistoryController {

    @Autowired
    private LitemallAccountService accountService;
    @Autowired
    private LitemallAccountHistoryService accountHistoryService;

    @GetMapping("/list")
    public Object list(@AdminLoginUser Integer currentUserId, Byte type, String detail,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        LitemallAccountHistory accountHistory = new LitemallAccountHistory();
        accountHistory.setAdminId(currentUserId);
        accountHistory.setAccountType(AccountUtil.AccountType.PROFIT.getAccountType());
        try {
            accountHistory.setType(AccountUtil.Type.getType(type).getType());
        } catch (Exception e) {
        }
        if (StringUtils.hasText(detail)) {
            accountHistory.setDetail(detail);
        }
        List<LitemallAccountHistory> accountHistoryList = accountHistoryService.findByAccountHistorySelective(accountHistory, page, limit, sort, order);
        return ResponseUtil.okList(accountHistoryList);
    }
}
