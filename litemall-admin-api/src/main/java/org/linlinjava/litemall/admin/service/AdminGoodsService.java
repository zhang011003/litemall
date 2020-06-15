package org.linlinjava.litemall.admin.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.linlinjava.litemall.admin.dto.Goods;
import org.linlinjava.litemall.admin.dto.GoodsAllinone;
import org.linlinjava.litemall.admin.dto.GoodsProductPrice;
import org.linlinjava.litemall.admin.vo.CatVo;
import org.linlinjava.litemall.admin.vo.GoodsProductAgentVo;
import org.linlinjava.litemall.admin.vo.GoodsProductVo;
import org.linlinjava.litemall.admin.vo.GoodsVo;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.linlinjava.litemall.admin.util.AdminResponseCode.GOODS_NAME_EXIST;

@Service
@Slf4j
public class AdminGoodsService {
//    private final Log logger = LogFactory.getLog(AdminGoodsService.class);

    @Autowired
    private LitemallGoodsService goodsService;
    @Autowired
    private LitemallGoodsSpecificationService specificationService;
    @Autowired
    private LitemallGoodsAttributeService attributeService;
    @Autowired
    private LitemallGoodsProductService productService;
    @Autowired
    private LitemallCategoryService categoryService;
    @Autowired
    private LitemallBrandService brandService;
    @Autowired
    private LitemallCartService cartService;
    @Autowired
    private QCodeService qCodeService;
    @Autowired
    private LitemallGoodsProductAgentService goodsProductAgentService;
    @Autowired
    private LitemallGoodsExtraInfoService goodsExtraInfoService;

    public Object list(Goods goods,
                       Integer page, Integer limit, String sort, String order) {
        List<LitemallGoods> goodsList = Lists.newArrayList();

        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        Map<Integer, List<LitemallGoodsProductAgent>> gpaMap = goodsProductAgentService.queryByAgentId(litemallAdmin.getId())
                .stream().collect(Collectors.toMap(LitemallGoodsProductAgent::getGoodsId, agent -> Lists.newArrayList(agent), (a, b) -> {
                    a.addAll(b);
                    return a;
                }));

        // 非管理员，从agent表获取
        if (litemallAdmin.getParent() != null) {
            List<Integer> goodsIdList = Lists.newArrayList(gpaMap.keySet());
            if (goodsIdList.size() > 0) {
                if (goods.getGoodsId() != null) {
                    if (goodsIdList.contains(goods.getGoodsId())) {
                        goodsList = goodsService.querySelective(goods.getGoodsId(),goods.getGoodsSn(), goods.getName(), page, limit, sort, order);
                    } else {
                        goodsList = Lists.newArrayList();
                    }
                } else {
                    goodsList = goodsService.querySelective(goodsIdList, goods.getGoodsSn(), goods.getName(), page, limit, sort, order);

                }
            } else {
                log.info("agent表中没有相应记录");
            }
        } else {
            goodsList = goodsService.querySelective(goods.getGoodsId(), goods.getGoodsSn(), goods.getName(), page, limit, sort, order);
        }

        // 重新设置当前价格
        if (goodsList != null) {
            goodsList.forEach(litemallGoods -> {
                Optional<LitemallGoodsProductAgent> gpa = gpaMap.getOrDefault(litemallGoods.getId(), Lists.newArrayList()).stream()
                        .min(Comparator.comparing(LitemallGoodsProductAgent::getPrice));
                if (gpa.isPresent()) {
                    litemallGoods.setRetailPrice(gpa.get().getPrice());
                }
            });
        }

        List<Integer> goodsIdList = goodsList.stream().map(LitemallGoods::getId).collect(Collectors.toList());
        List<LitemallGoodsExtraInfo> extraInfoList = goodsExtraInfoService.queryByGoodsidsAndAdminId(goodsIdList, litemallAdmin.getId());
        Map<Integer, LitemallGoodsExtraInfo> extraInfoMap = extraInfoList.stream().collect(Collectors.toMap(LitemallGoodsExtraInfo::getGoodsId, Function.identity()));

        List<GoodsVo> goodsVoList = Lists.newArrayListWithCapacity(goodsList.size());
        for (LitemallGoods litemallGoods : goodsList) {
            GoodsVo goodsVo = new GoodsVo();
            BeanUtils.copyProperties(litemallGoods, goodsVo);
            goodsVo.setExtraInfo(extraInfoMap.get(litemallGoods.getId()));
            goodsVoList.add(goodsVo);
        }

        return ResponseUtil.okList(goodsVoList);
    }

    private Object validate(GoodsAllinone goodsAllinone) {
        LitemallGoods goods = goodsAllinone.getGoods();
        String name = goods.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        String goodsSn = goods.getGoodsSn();
        if (StringUtils.isEmpty(goodsSn)) {
            return ResponseUtil.badArgument();
        }
        // 品牌商可以不设置，如果设置则需要验证品牌商存在
        Integer brandId = goods.getBrandId();
        if (brandId != null && brandId != 0) {
            if (brandService.findById(brandId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }
        // 分类可以不设置，如果设置则需要验证分类存在
        Integer categoryId = goods.getCategoryId();
        if (categoryId != null && categoryId != 0) {
            if (categoryService.findById(categoryId) == null) {
                return ResponseUtil.badArgumentValue();
            }
        }

        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        for (LitemallGoodsAttribute attribute : attributes) {
            String attr = attribute.getAttribute();
            if (StringUtils.isEmpty(attr)) {
                return ResponseUtil.badArgument();
            }
            String value = attribute.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        for (LitemallGoodsSpecification specification : specifications) {
            String spec = specification.getSpecification();
            if (StringUtils.isEmpty(spec)) {
                return ResponseUtil.badArgument();
            }
            String value = specification.getValue();
            if (StringUtils.isEmpty(value)) {
                return ResponseUtil.badArgument();
            }
        }

        LitemallGoodsProduct[] products = goodsAllinone.getProducts();
        for (LitemallGoodsProduct product : products) {
            Integer number = product.getNumber();
            if (number == null || number < 0) {
                return ResponseUtil.badArgument();
            }

            BigDecimal price = product.getPrice();
            if (price == null) {
                return ResponseUtil.badArgument();
            }

            String[] productSpecifications = product.getSpecifications();
            if (productSpecifications.length == 0) {
                return ResponseUtil.badArgument();
            }
        }

        return null;
    }

    /**
     * 编辑商品
     *
     * NOTE：
     * 由于商品涉及到四个表，特别是litemall_goods_product表依赖litemall_goods_specification表，
     * 这导致允许所有字段都是可编辑会带来一些问题，因此这里商品编辑功能是受限制：
     * （1）litemall_goods表可以编辑字段；
     * （2）litemall_goods_specification表只能编辑pic_url字段，其他操作不支持；
     * （3）litemall_goods_product表只能编辑price, number和url字段，其他操作不支持；
     * （4）litemall_goods_attribute表支持编辑、添加和删除操作。
     *
     * NOTE2:
     * 前后端这里使用了一个小技巧：
     * 如果前端传来的update_time字段是空，则说明前端已经更新了某个记录，则这个记录会更新；
     * 否则说明这个记录没有编辑过，无需更新该记录。
     *
     * NOTE3:
     * （1）购物车缓存了一些商品信息，因此需要及时更新。
     * 目前这些字段是goods_sn, goods_name, price, pic_url。
     * （2）但是订单里面的商品信息则是不会更新。
     * 如果订单是未支付订单，此时仍然以旧的价格支付。
     */
    @Transactional
    public Object update(GoodsAllinone goodsAllinone) {
        Object error = validate(goodsAllinone);
        if (error != null) {
            return error;
        }

        LitemallGoods goods = goodsAllinone.getGoods();
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        goods.setShareUrl(url);

        // 商品表里面有一个字段retailPrice记录当前商品的最低价
        // TODO: 更新商品时， 货品价格是否需要从agent表获取？
        goods.setRetailPrice(getRetailPrice(products));
        
        // 商品基本信息表litemall_goods
        if (goodsService.updateById(goods) == 0) {
            throw new RuntimeException("更新数据失败");
        }

        Integer gid = goods.getId();

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            // 目前只支持更新规格表的图片字段
            if(specification.getUpdateTime() == null){
                specification.setSpecification(null);
                specification.setValue(null);
                specificationService.updateById(specification);
            }
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            if(product.getUpdateTime() == null) {
                productService.updateById(product);
            }
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            if (attribute.getId() == null || attribute.getId().equals(0)){
                attribute.setGoodsId(goods.getId());
                attributeService.add(attribute);
            }
            else if(attribute.getDeleted()){
                attributeService.deleteById(attribute.getId());
            }
            else if(attribute.getUpdateTime() == null){
                attributeService.updateById(attribute);
            }
        }

        // 这里需要注意的是购物车litemall_cart有些字段是拷贝商品的一些字段，因此需要及时更新
        // 目前这些字段是goods_sn, goods_name, price, pic_url
        for (LitemallGoodsProduct product : products) {
            cartService.updateProduct(product.getId(), goods.getGoodsSn(), goods.getName(), product.getPrice(), product.getUrl());
        }

        return ResponseUtil.ok();
    }

    @Transactional
    public Object delete(LitemallGoods goods) {
        Integer id = goods.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }

        Integer gid = goods.getId();
        goodsService.deleteById(gid);
        specificationService.deleteByGid(gid);
        attributeService.deleteByGid(gid);
        productService.deleteByGid(gid);
        return ResponseUtil.ok();
    }

    @Transactional
    public Object create(GoodsAllinone goodsAllinone) {
        Object error = validate(goodsAllinone);
        if (error != null) {
            return error;
        }

        LitemallGoods goods = goodsAllinone.getGoods();
        LitemallGoodsAttribute[] attributes = goodsAllinone.getAttributes();
        LitemallGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
        LitemallGoodsProduct[] products = goodsAllinone.getProducts();

        String name = goods.getName();
        if (goodsService.checkExistByName(name)) {
            return ResponseUtil.fail(GOODS_NAME_EXIST, "商品名已经存在");
        }
        BigDecimal retailPrice = getRetailPrice(products);

        goods.setRetailPrice(retailPrice);

        // 商品基本信息表litemall_goods
        goodsService.add(goods);

        //将生成的分享图片地址写入数据库
        String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());
        if (!StringUtils.isEmpty(url)) {
            goods.setShareUrl(url);
            if (goodsService.updateById(goods) == 0) {
                throw new RuntimeException("更新数据失败");
            }
        }

        // 商品规格表litemall_goods_specification
        for (LitemallGoodsSpecification specification : specifications) {
            specification.setGoodsId(goods.getId());
            specificationService.add(specification);
        }

        // 商品参数表litemall_goods_attribute
        for (LitemallGoodsAttribute attribute : attributes) {
            attribute.setGoodsId(goods.getId());
            attributeService.add(attribute);
        }

        // 商品货品表litemall_product
        for (LitemallGoodsProduct product : products) {
            product.setGoodsId(goods.getId());
            productService.add(product);
        }
        return ResponseUtil.ok();
    }

    private BigDecimal getRetailPrice(LitemallGoodsProduct[] goodsProducts) {
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        for (LitemallGoodsProduct product : goodsProducts) {
            BigDecimal productPrice = product.getPrice();
            if (retailPrice.compareTo(productPrice) == 1) {
                retailPrice = productPrice;
            }
        }
        return retailPrice;
    }
    /**
     * 商品表里面有一个字段retailPrice记录当前商品的最低价
     * @param goodsId
     * @param adminId
     * @return
     */
    private BigDecimal getRetailPrice(Integer goodsId, Integer adminId) {
        List<LitemallGoodsProductAgent> agents = goodsProductAgentService.queryByGidAndAgentId(goodsId, adminId);
        BigDecimal retailPrice = new BigDecimal(Integer.MAX_VALUE);
        if (agents.size() > 0) {
            for (LitemallGoodsProductAgent agent : agents) {
                BigDecimal productPrice = agent.getPrice();
                if (retailPrice.compareTo(productPrice) == 1) {
                    retailPrice = productPrice;
                }
            }
        } else {
            List<LitemallGoodsProduct> goodsProductList = productService.queryByGid(goodsId);

            for (LitemallGoodsProduct product : goodsProductList) {
                BigDecimal productPrice = product.getPrice();
                if (retailPrice.compareTo(productPrice) == 1) {
                    retailPrice = productPrice;
                }
            }
        }

        return retailPrice;
    }

    public Object list2() {
        // http://element-cn.eleme.io/#/zh-CN/component/cascader
        // 管理员设置“所属分类”
        List<LitemallCategory> l1CatList = categoryService.queryL1();
        List<CatVo> categoryList = new ArrayList<>(l1CatList.size());

        for (LitemallCategory l1 : l1CatList) {
            CatVo l1CatVo = new CatVo();
            l1CatVo.setValue(l1.getId());
            l1CatVo.setLabel(l1.getName());

            List<LitemallCategory> l2CatList = categoryService.queryByPid(l1.getId());
            List<CatVo> children = new ArrayList<>(l2CatList.size());
            for (LitemallCategory l2 : l2CatList) {
                CatVo l2CatVo = new CatVo();
                l2CatVo.setValue(l2.getId());
                l2CatVo.setLabel(l2.getName());
                children.add(l2CatVo);
            }
            l1CatVo.setChildren(children);

            categoryList.add(l1CatVo);
        }

        // http://element-cn.eleme.io/#/zh-CN/component/select
        // 管理员设置“所属品牌商”
        List<LitemallBrand> list = brandService.all();
        List<Map<String, Object>> brandList = new ArrayList<>(l1CatList.size());
        for (LitemallBrand brand : list) {
            Map<String, Object> b = new HashMap<>(2);
            b.put("value", brand.getId());
            b.put("label", brand.getName());
            brandList.add(b);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("categoryList", categoryList);
        data.put("brandList", brandList);
        return ResponseUtil.ok(data);
    }

    public Object detail(Integer id) {

        LitemallGoods goods = goodsService.findById(id);

        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        List<LitemallGoodsProduct> tmpProducts = productService.queryByGid(id);
        List<GoodsProductVo> gpVoList = tmpProducts.stream().map(product -> {
            GoodsProductVo gpaVo = new GoodsProductVo();
            BeanUtils.copyProperties(product, gpaVo);
            return gpaVo;
        }).collect(Collectors.toList());

        // 可能存在多次派货的情况
        Map<Integer, List<LitemallGoodsProductAgent>> gpaMaps = goodsProductAgentService.queryByGidAndAgentId(id, litemallAdmin.getId())
                .stream().collect(Collectors.toMap(LitemallGoodsProductAgent::getGoodsProductId, gpa -> Lists.newArrayList(gpa), (a, b) -> {
                    a.addAll(b);
                    return a;
                }));
        Map<Integer, GoodsProductVo> gpVoMap = gpVoList.stream().collect(Collectors.toMap(GoodsProductVo::getId, Function.identity()));

        gpaMaps.entrySet().stream().forEach(entry -> {
            GoodsProductVo gpVo = gpVoMap.get(entry.getKey());
            List<LitemallGoodsProductAgent> gpaList = entry.getValue();

            // 如果派货只有一次，则修改对应的值
            // 如果派货不止一次，则需要增加新的GoodsProductVo对应多次派货的情况
            for (int i = 0; i < gpaList.size(); i++) {
                LitemallGoodsProductAgent gpa = gpaList.get(i);
                if (i == 0) {
                    gpVo.setBasePrice(gpa.getBasePrice());
                    gpVo.setNumber(gpa.getNumber());
                    gpVo.setPrice(gpa.getPrice());
                    gpVo.setDispatchPrice(gpa.getDispatchPrice());
                } else {
                    GoodsProductVo gpVo2 = new GoodsProductVo();
                    BeanUtils.copyProperties(gpVo, gpVo2);
                    gpVo2.setBasePrice(gpa.getBasePrice());
                    gpVo2.setNumber(gpa.getNumber());
                    gpVo2.setPrice(gpa.getPrice());
                    gpVo2.setDispatchPrice(gpa.getDispatchPrice());
                    gpVoList.add(gpVo2);
                }
            }
        });

//        // 设置派货价格
//        List<Integer> productIds = gpVoList.stream().map(GoodsProductVo::getId).collect(Collectors.toList());
//        // 可能查出相同productId，相同adminId，但不同成本价格的数据，但派货价格是一样的
//        List<LitemallGoodsProductAgent> agents = goodsProductAgentService.queryByProductIds(productIds,
//                litemallAdmin.getId(), LitemallGoodsProductAgent.Column.goodsProductId,
//                LitemallGoodsProductAgent.Column.dispatchPrice);
//        Map<Integer, List<LitemallGoodsProductAgent>> agentMap = agents.stream().collect(
//                Collectors.toMap(LitemallGoodsProductAgent::getGoodsProductId, Lists::newArrayList, (a, b) -> {
//                    a.addAll(b);
//                    return a;
//                }));
//        for (GoodsProductVo gpVo : gpVoList) {
//            List<LitemallGoodsProductAgent> agentList = agentMap.get(gpVo.getId());
//            if (agentList != null && agentList.size()>0) {
//                gpVo.setDispatchPrice(agentList.get(0).getDispatchPrice());
//            }
//        }

        List<LitemallGoodsSpecification> specifications = specificationService.queryByGid(id);
        List<LitemallGoodsAttribute> attributes = attributeService.queryByGid(id);

        Integer categoryId = goods.getCategoryId();
        LitemallCategory category = categoryService.findById(categoryId);
        Integer[] categoryIds = new Integer[]{};
        if (category != null) {
            Integer parentCategoryId = category.getPid();
            categoryIds = new Integer[]{parentCategoryId, categoryId};
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("specifications", specifications);
        data.put("products", gpVoList);
        data.put("attributes", attributes);
        data.put("categoryIds", categoryIds);

        return ResponseUtil.ok(data);
    }

    @Transactional
    public Object updatePrice(GoodsProductPrice goodsProductPrice) {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();

        // 更新货品信息
        LitemallGoodsProduct goodsProduct = new LitemallGoodsProduct();
        goodsProduct.setId(goodsProductPrice.getId());
        // 只有可以编辑商品的权限，才可以修改库存
        try {
            SecurityUtils.getSubject().checkPermission("admin:goods:update");
        } catch (AuthorizationException e) {
            goodsProductPrice.setNumber(null);
        }
        if (goodsProductPrice.getNumber() != null) {
            goodsProduct.setNumber(goodsProductPrice.getNumber());
            productService.updateById(goodsProduct);
        }

        // 更新agent表信息
//            LitemallGoodsProductExtraInfo goodsProductExtraInfo = new LitemallGoodsProductExtraInfo();
//            BeanUtils.copyProperties(goodsProductPrice, goodsProductExtraInfo, "id");
//            goodsProductExtraInfo.setAdminId(litemallAdmin.getId());
//            goodsProductExtraInfoService.saveOrUpdate(goodsProductExtraInfo);
        LitemallGoodsProductAgent agent = new LitemallGoodsProductAgent();
        BeanUtils.copyProperties(goodsProductPrice, agent, "id", "basePrice");
        agent.setGoodsProductId(goodsProductPrice.getId());
        agent.setAgentId(litemallAdmin.getId());
        goodsProductAgentService.updatePrice(agent);

        // 更新商品的retailPrice
        Integer goodsId = goodsProductPrice.getGoodsId();
        LitemallGoods goods = new LitemallGoods();
        goods.setId(goodsId);
        goods.setRetailPrice(getRetailPrice(goodsId, litemallAdmin.getId()));
        goodsService.updateById(goods);
        return ResponseUtil.ok();
    }

    public Object changeShow(Integer id, Boolean isShow) {
        LitemallAdmin litemallAdmin = (LitemallAdmin) SecurityUtils.getSubject().getPrincipal();
        LitemallGoodsExtraInfo extraInfo = new LitemallGoodsExtraInfo();
        extraInfo.setGoodsId(id);
        extraInfo.setAdminId(litemallAdmin.getId());
        extraInfo.setIsShow(isShow);
        goodsExtraInfoService.saveOrUpdateGoodsExtraInfo(extraInfo);
        return ResponseUtil.ok();
    }
}
