package org.linlinjava.litemall.db.dao;

import org.apache.ibatis.annotations.Param;

public interface GoodsProductExtraInfoMapper {
    int addStock(@Param("id") Integer id, @Param("num") Integer num);
    int reduceStock(@Param("id") Integer id, @Param("num") Integer num);
}