package com.soyoung.st.dao;

import com.soyoung.st.model.SampleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SampleDao {

    @Select("select id,name,amount from rptest where mod(id,#{shardingTotal})=#{shardingItem}")
    List<SampleInfo> querySimpleList(Map<String, Object> param);
}
