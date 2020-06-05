package org.linlinjava.litemall.db.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface AccountMapper {
    int updateAccount(@Param("adminId") Integer adminId, @Param("balance") BigDecimal balance, @Param("isAdd")boolean isAdd);
}