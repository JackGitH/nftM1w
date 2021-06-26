package com.cxnb.mapper;

import com.cxnb.entity.DropOne;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DropMapper {
    // 查看链类型 status=0 代表不判断状态
    List<DropOne> list(@Param("status") Integer status,@Param("chainid") Long chainid,@Param("from") String from,@Param("contractaddress") String contractaddress);

    DropOne load(@Param("status") Integer status, @Param("id") Long id);

    void add(DropOne dropOne);

    void delete(@Param("pk") String pk);

    void update(DropOne dropOne);
}
