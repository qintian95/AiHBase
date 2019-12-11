package com.ai.dao;

import com.ai.pojo.ChargeTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 15:39
 */
@Mapper
public interface ChargeTreeMapper {

    @Select("select * from charge_tree")
    List<ChargeTree> getChargeTree();
}
