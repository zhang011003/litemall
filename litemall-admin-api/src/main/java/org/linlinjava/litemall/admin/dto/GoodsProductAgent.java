package org.linlinjava.litemall.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsProductAgent {
    private Integer goodsId;
    private Integer goodsProductId;
    private BigDecimal price;
    private BigDecimal basePrice;
    private BigDecimal dispatchPrice;
    private Integer number;
    private Integer dispatchNumber;
    private Integer agentId;

}
