// **********************************************************************
// This file was generated by a TARS parser!
// TARS version 1.0.1.
// **********************************************************************

package com.soyoung.st.model;

import java.io.Serializable;

public class Serial implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9205070609157989956L;
	
	private long id = 0L;
	private long serial = 0L;
	private String createTime = "";
	private String modifyTime = "";

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getSerial() {
		return serial;
	}

	public void setSerial(long serial) {
		this.serial = serial;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Serial() {
	}

	public Serial(long serial, String createTime, String modifyTime) {
		this.serial = serial;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
	}

}
