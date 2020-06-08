package org.linlinjava.litemall.admin.web;

import com.google.common.collect.Lists;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.admin.annotation.annotation.AdminLoginUser;
import org.linlinjava.litemall.admin.dto.GoodsProductAgent;
import org.linlinjava.litemall.admin.service.AdminGoodsProductAgentService;
import org.linlinjava.litemall.admin.vo.GoodsProductAgentVo;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallAdmin;
import org.linlinjava.litemall.db.domain.LitemallGoodsProduct;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;
import org.linlinjava.litemall.db.service.LitemallAdminService;
import org.linlinjava.litemall.db.service.LitemallGoodsProductAgentService;
import org.linlinjava.litemall.db.service.LitemallGoodsProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @RequiresPermissions("admin:goodsproductagent:dispatch")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "派货")
    @PostMapping("dispatch")
    public Object dispachProduct(@AdminLoginUser Integer currentUserId,
                               @RequestBody List<GoodsProductAgent> goodsProducts) {
        return goodsProductAgentService.dispachProduct(currentUserId, goodsProducts);
    }
}
