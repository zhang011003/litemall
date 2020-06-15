package org.linlinjava.litemall.admin.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsProductVo {
    private Integer id;
    private Integer goodsId;
    private String[] specifications;
    private BigDecimal price;
    private BigDecimal basePrice;
    private Integer number;
    private String url;
    private BigDecimal dispatchPrice;
}
