package org.linlinjava.litemall.admin.dto;

import lombok.Data;
import org.linlinjava.litemall.db.domain.LitemallGoods;
import org.linlinjava.litemall.db.domain.LitemallGoodsDispatchHistory;
import org.linlinjava.litemall.db.domain.LitemallGoodsProduct;

@Data
public class GoodsDispatchHistory {
    private LitemallGoodsDispatchHistory history;
    private LitemallGoods goods;
    private LitemallGoodsProduct product;
    private String userName;
    private String nickName;
}
