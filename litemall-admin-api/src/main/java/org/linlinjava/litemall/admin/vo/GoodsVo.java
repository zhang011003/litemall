package org.linlinjava.litemall.admin.vo;

import lombok.Data;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallGoodsExtraInfo;

import java.math.BigDecimal;

@Data
public class GoodsVo extends LitemallGoods {
    private LitemallGoodsExtraInfo extraInfo;
}
