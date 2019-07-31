package com.soyoung.st.exception;

public class BusinessException extends RuntimeException {


    private Integer code;
    private String msg;

    public BusinessException(Integer code,String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String message){
        super(message);
    }

    public BusinessException(BusinessError error){
        super(error.errorMsg());
        this.code = error.errorCode();
        this.msg = error.errorMsg();
    }

}
