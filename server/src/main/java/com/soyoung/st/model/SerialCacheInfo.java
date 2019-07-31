package com.soyoung.st.model;

public class SerialCacheInfo {

    private long filep = 0;
    private long fileEnd = 0;
    private long dbStart = 0;
    private long dbEnd = 0;

    public SerialCacheInfo(){

    }

    public SerialCacheInfo(long filep, long fileEnd, long dbStart, long dbEnd){
        this.filep = filep;
        this.fileEnd = fileEnd;
        this.dbStart = dbStart;
        this.dbEnd = dbEnd;
    }

    public long getFilep() {
        return filep;
    }

    public void setFilep(long filep) {
        this.filep = filep;
    }

    public long getFileEnd() {
        return fileEnd;
    }

    public void setFileEnd(long fileEnd) {
        this.fileEnd = fileEnd;
    }

    public long getDbStart() {
        return dbStart;
    }

    public void setDbStart(long dbStart) {
        this.dbStart = dbStart;
    }

    public long getDbEnd() {
        return dbEnd;
    }

    public void setDbEnd(long dbEnd) {
        this.dbEnd = dbEnd;
    }
}
