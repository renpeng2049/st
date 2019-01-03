package com.soyoung.st.netty;

/** 和操作系统交互的数据结构 */
public class ChannelOsRsp {

	private String errcode;
	private String errmsg;
	private String result_code;     //命令错误码
	private String standard_output; //标准输出流
	private String error_output;    //错误输出流
	public String getResult_code() {
		return result_code;
	}
	public void setResult_code(String result_code) {
		this.result_code = result_code;
	}
	public String getStandard_output() {
		return standard_output;
	}
	public void setStandard_output(String standard_output) {
		this.standard_output = standard_output;
	}
	public String getError_output() {
		return error_output;
	}
	public void setError_output(String error_output) {
		this.error_output = error_output;
	}

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
}
