package org.linlinjava.litemall.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminAccountTransfer {
    private Integer transferTo;
    private BigDecimal money;
}
