package com.soyoung.st.dao;

import com.soyoung.st.model.SampleInfo;
import com.soyoung.st.model.Serial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SampleDao {

    @Select("select id,name,amount from rptest where mod(id,#{shardingTotal})=#{shardingItem}")
    List<SampleInfo> querySimpleList(Map<String, Object> param);

    @Select("select Fid,Fserial,FcreateTime,FmodifyTime from t_serial order by FmodifyTime desc limit 1 for update")
    @Results(id="RelationResultMap",value = {
            @Result(property = "id",column ="Fid"),
            @Result(property = "serial",column ="Fserial"),
            @Result(property = "createTime",column ="FcreateTime"),
            @Result(property = "modifyTime",column ="FmodifyTime")})
    public Serial querySerialForUpdate();

}
