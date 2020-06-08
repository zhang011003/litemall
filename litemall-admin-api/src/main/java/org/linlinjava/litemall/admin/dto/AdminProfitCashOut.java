package org.linlinjava.litemall.admin.dto;

import lombok.Data;
import org.linlinjava.litemall.db.domain.LitemallAdmin;

import java.math.BigDecimal;

@Data
public class AdminProfitCashOut {
    private BigDecimal money;
    private LitemallAdmin admin;
}
