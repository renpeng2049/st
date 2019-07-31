package com.soyoung.st.dao;

import com.soyoung.st.model.Serial;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SerialDAO {
	@Select("select Fid,Fserial,FcreateTime,FmodifyTime from t_serial order by FmodifyTime desc limit 1 for update")
	@Results(id="RelationResultMap",value = { 
			@Result(property = "id",column ="Fid"),
			@Result(property = "serial",column ="Fserial"),
			@Result(property = "createTime",column ="FcreateTime"),
			@Result(property = "modifyTime",column ="FmodifyTime")})
	public Serial querySerialForUpdate();

	@Update("update t_serial set Fserial = #{serial}, FmodifyTime = now() where Fid = #{id} and Fserial = #{lastserial}")
	public int updateSerial(@Param("serial") long serial, @Param("id") long id, @Param("lastserial") long lastserial);
}
