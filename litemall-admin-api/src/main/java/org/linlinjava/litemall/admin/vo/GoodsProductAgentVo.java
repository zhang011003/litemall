package org.linlinjava.litemall.admin.vo;

import lombok.Builder;
import lombok.Data;
import org.linlinjava.litemall.db.domain.LitemallGoodsProduct;
import org.linlinjava.litemall.db.domain.LitemallGoodsProductAgent;

@Data
@Builder
public class GoodsProductAgentVo {
    private LitemallGoodsProductAgent agent;
    private LitemallGoodsProduct goodsProduct;
}
