package org.linlinjava.litemall.admin.dto;

import lombok.Data;

@Data
public class Goods {
    private Integer goodsId;
    private String goodsSn;
    private String name;
    private boolean onSale;
    private boolean isNew;
    private boolean hot;
}
