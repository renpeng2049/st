package com.soyoung.st.exception;

/**
 * 返回明确的错误码给事务管理器
 */
public enum BusinessError {
    //未知错误
    UNKNOWN_EXCEPTION(9999, "unknown exception."),

    //序列号层面报错
    INVALID_SERIAL_DB_STEP(2001, "invalid serial db step."),
    INVALID_SERIAL_FILE_STEP(2002, "invalid serial file step."),
    SERIAL_DBSTEP_SHOULDBE_LARGER_THAN_FILESTEP(2003, "SerialDBStep should be larger than SerialFileStep."),
    SERIAL_FILE_DOESNT_EXIST(2004, "Serial file does't exist."),
    CREATE_SERIAL_FILE_FAILED(2005, "Create serial file failed."),
    INVALID_FILE_CHECKSUM(2006, "invalid fileCheckSum."),
    LOCK_SERIAL_FILE_FAILED(2007, "Lock serial file failed."),
    LOAD_SERIAL_FILE_FAILED(2008, "Load serial file failed."),
    WRITE_SERIAL_FILE_FAILED(2009, "Write serial file failed."),
    INVALID_SERIALFILE_PATH(2010, "invalid serial file path."),
    DB_SERIAL_ROW_IS_EMPTY(2011, "DB serial row is empty."),
    INVALID_REQUEST_CURRENT_MAX_SERIAL_NUMBER(2012, "Request current max serial is invalid."),
    REQUEST_CURRENT_MAX_SERIALNUMBER_LARGERTHAN_FILECURRENTSERIAL(2013, "Request currentMaxSerialNumber is larger than fileCurrentSerial."),
    INVALID_SERIALFILE_CONTENT_INITVALUE(2014, "invalid serial file content init value."),
    FILE_SERIAL_IS_INCONSISTENT_WITH_DB(2015, "file serial is inconsistent with DB."),
    FREELOCK_SERIAL_FILE_FAILED(2016, "FreeLock serial file failed."),
    MEMORY_CURRENT_MAX_SERIAL_LARGERTHAN_FILECURRENTSERIAL(2017, "memoryMaxSerialNumber is larger than fileCurrentSerial."),
    SERIAL_CONFIG_PARAM_INVALID(2018, "reqNum is too large or serial config param invalid."),
    CLOSE_SERIAL_FILE_RESOURCE_ERROR(2019, "close serial file resources error"),
    INVALID_SERIAL_FILE_CONTENT(2020, "invalid serial file content"),
    SERIAL_CACHE_ERROR(2021, "serial cache error"),

    ;


    private int errorCode;

    private String errMsg;

    BusinessError(int errorCode, String errMsg) {
        this.errorCode = errorCode;
        this.errMsg = errMsg;
    }

    public int errorCode() {
        return errorCode;
    }

    public String errorMsg() {
        return errMsg;
    }

    public String toString() {
        return String.valueOf(this.errorCode + ":" + this.errMsg);
    }
}
