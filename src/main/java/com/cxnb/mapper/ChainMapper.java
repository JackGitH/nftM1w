package com.cxnb.mapper;

import com.cxnb.entity.Account;
import com.cxnb.entity.Chain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChainMapper {
    // 查看链类型 status=0 代表不判断状态
    List<Chain> list(@Param("status") Integer status);

    Chain load(@Param("status") Integer status, @Param("id") Long id);

    void add(Chain chain);

    void delete(@Param("id") Long id);

    void update(Chain chain);
}
