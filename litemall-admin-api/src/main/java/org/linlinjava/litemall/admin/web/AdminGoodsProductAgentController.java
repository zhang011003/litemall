package org.linlinjava.litemall.admin.web;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.admin.dto.Goods;
import org.linlinjava.litemall.admin.dto.GoodsProductAgent;
import org.linlinjava.litemall.admin.service.AdminGoodsDispatchHistoryService;
import org.linlinjava.litemall.admin.service.AdminGoodsProductAgentService;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallGoodsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/agent")
@Validated
public class AdminGoodsProductAgentController {

    @Autowired
    private LitemallAdminService adminService;
    @Autowired
    private LitemallGoodsProductService goodsProductService;
    @Autowired
    private AdminGoodsProductAgentService goodsProductAgentService;
    @Autowired
    private AdminGoodsDispatchHistoryService goodsDispatchHistoryService;

    @RequiresPermissions("admin:goodsproductagent:dispatch")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "派货")
    @PostMapping("dispatch")
    public Object dispachProduct(@AdminLoginUser Integer currentUserId,
                               @RequestBody List<GoodsProductAgent> goodsProducts) {
        return goodsProductAgentService.dispachProduct(currentUserId, goodsProducts);
    }

    @GetMapping("dispatchHistory/list")
    public Object list(Goods goods, @RequestParam(defaultValue = "1") Integer type,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        return goodsDispatchHistoryService.list(goods, type, page, limit, sort, order);
    }
}
