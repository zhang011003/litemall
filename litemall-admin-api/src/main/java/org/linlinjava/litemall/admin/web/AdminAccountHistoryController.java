package org.linlinjava.litemall.admin.web;

import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallAccount;
import org.linlinjava.litemall.db.domain.LitemallAccountHistory;
import org.linlinjava.litemall.db.service.LitemallAccountHistoryService;
import org.linlinjava.litemall.db.service.LitemallAccountService;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallNoticeAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/accounthistory")
@Validated
public class AdminAccountHistoryController {

    @Autowired
    private LitemallAccountService accountService;
    @Autowired
    private LitemallAccountHistoryService accountHistoryService;

    @GetMapping("/list")
    public Object list(@AdminLoginUser Integer currentUserId, Integer type, String detail,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        LitemallAccountHistory accountHistory = new LitemallAccountHistory();
        accountHistory.setAdminId(currentUserId);
        if (type == 1 || type == 2) {
            accountHistory.setType(type.byteValue());
        }
        if (StringUtils.hasText(detail)) {
            accountHistory.setDetail(detail);
        }
        List<LitemallAccountHistory> accountHistoryList = accountHistoryService.findByAccountHistorySelective(accountHistory, page, limit, sort, order);
        return ResponseUtil.okList(accountHistoryList);
    }
}
