package org.linlinjava.litemall.admin.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Data
public class GoodsProductAgent {
    private Integer goodsId;
    private Integer goodsProductId;
    private BigDecimal price;
    @EqualsAndHashCode.Exclude
    private BigDecimal basePrice;
    private BigDecimal dispatchPrice;
    @EqualsAndHashCode.Exclude
    private Integer number;
    private Integer dispatchNumber;
    private Integer agentId;
}
