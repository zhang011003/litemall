package org.linlinjava.litemall.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Data
public class GoodsProductAgent {
    private Integer goodsId;
    private Integer goodsProductId;
    private BigDecimal price;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BigDecimal basePrice;
    private BigDecimal dispatchPrice;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Integer number;
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Integer dispatchNumber;
    private Integer agentId;
}
