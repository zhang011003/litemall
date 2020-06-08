package org.linlinjava.litemall.db.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AccountMapper {
    int updateAccountWithOptimisticLocker(@Param("adminId") Integer adminId,
                                          @Param("balance") BigDecimal balance,
                                          @Param("isAdd")boolean isAdd,
                                          @Param("lastUpdateTime") LocalDateTime lastUpdateTime);
}