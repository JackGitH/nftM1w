package com.cxnb.mapper;

import com.cxnb.entity.Account;
import com.cxnb.entity.Chain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {
    // 查看链类型 status=0 代表不判断状态
    List<Account> list(@Param("status") Integer status);

    Account load(@Param("status") Integer status,@Param("pub") String pub);
    void add(Account account);


    void delete(@Param("pk") String pk);

    void update(Account account);
}
