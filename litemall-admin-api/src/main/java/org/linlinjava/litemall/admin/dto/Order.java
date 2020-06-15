package org.linlinjava.litemall.admin.dto;

import lombok.Data;
import org.linlinjava.litemall.db.domain.LitemallOrder;

@Data
public class Order extends LitemallOrder {
    private String namePath;
}
