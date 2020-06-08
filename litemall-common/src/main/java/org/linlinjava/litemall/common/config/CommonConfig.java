package org.linlinjava.litemall.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {
    @Value("${litemall.goods-distribution}")
    private Boolean goodsDistribution;

    public boolean supportGoodsDistribution() {
        return goodsDistribution != null && goodsDistribution;
    }
}


