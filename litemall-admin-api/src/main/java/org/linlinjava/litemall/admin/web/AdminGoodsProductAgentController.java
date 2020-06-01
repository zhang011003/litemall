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

//    private LitemallGoodsProductAgentService goodsProductAgentService;

    @Autowired
    private AdminGoodsProductAgentService goodsProductAgentService;
//    @RequiresPermissions("admin:goodsproductagent:list")
//    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "代理商查询")
//    @GetMapping("list")
//    public Object list(@AdminLoginUser Integer currentUserId,
//                     @RequestParam(defaultValue = "1") Integer page,
//                     @RequestParam(defaultValue = "10") Integer limit,
//                     @Sort @RequestParam(defaultValue = "add_time") String sort,
//                     @Order @RequestParam(defaultValue = "desc") String order) {
//
//        List<LitemallGoodsProductAgent> agentList = goodsProductAgentService.list(currentUserId, page, limit, sort, order);
//        List<GoodsProductAgentVo> agentVos = Lists.newArrayListWithCapacity(agentList.size());
//        List<Integer> gpIds = agentList.stream().map(LitemallGoodsProductAgent::getGoodsProductId).collect(Collectors.toList());
//        Map<Integer, LitemallGoodsProduct> goodsProductMap = goodsProductService.findByIds(gpIds).stream()
//                .collect(Collectors.toMap(LitemallGoodsProduct::getId, Function.identity()));
//
//        agentList.forEach(agent -> {
//            GoodsProductAgentVo agentVo = new GoodsProductAgentVo();
//            agentVo.setAgent(agent);
//            agentVo.setGoodsProduct(goodsProductMap.get(agent.getGoodsProductId()));
//            agentVos.add(agentVo);
//        });
//        return ResponseUtil.ok(agentVos);
//    }

    @RequiresPermissions("admin:goodsproductagent:dispatch")
    @RequiresPermissionsDesc(menu = {"商品管理", "商品管理"}, button = "派货")
    @PostMapping("dispatch")
    public Object dispachProduct(@AdminLoginUser Integer currentUserId,
                               @RequestBody List<GoodsProductAgent> goodsProducts) {
        return goodsProductAgentService.dispachProduct(currentUserId, goodsProducts);
    }
}
