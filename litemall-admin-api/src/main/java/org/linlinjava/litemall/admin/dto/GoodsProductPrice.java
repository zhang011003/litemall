package org.linlinjava.litemall.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsProductPrice {
    /**
     * productId
     */
    private Integer id;
    /**
     * goodsId
     */
    private Integer goodsId;
    /**
     * 货品库存
     */
    private Integer number;
    /**
     * 派货价格
     */
    private BigDecimal dispatchPrice;
    /**
     * 销售价格
     */
    private BigDecimal price;
    /**
     * 基础价格（成本价格）
     */
    private BigDecimal basePrice;
}
