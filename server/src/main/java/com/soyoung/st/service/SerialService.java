package com.soyoung.st.service;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.soyoung.st.dao.SerialDAO;
import com.soyoung.st.exception.BusinessError;
import com.soyoung.st.exception.BusinessException;
import com.soyoung.st.model.Serial;
import com.soyoung.st.model.SerialCacheInfo;
import com.soyoung.st.utils.SerialFileOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

@Service
public class SerialService {

    private static final Logger LG = LoggerFactory.getLogger("SerialService");

    /**
     * 文件内容缓冲区256位，包含4个long型字段
     * filep    当前读数
     * fileend  当前文件最大数
     * dbstart  db取出的起始数
     * dbend    db取出的最大数
     *
     * 当文件中预读的剩余数量少于阈值，会触发新的预读，此时更新dbstart和dbend
     */
    private static final int BUFFSIZE = 32;

    private static final int COLUMNSIZE = 8;

    private static final long MIN_SERIAL = 1000000000L;

    private static final String CACHE_NAME = "SERIAL_CACHE";

    private LoadingCache<String, SerialCacheInfo> loadingCache = CacheBuilder.newBuilder().build(new CacheLoader<String, SerialCacheInfo>() {
        @Override
        public SerialCacheInfo load(String key) throws Exception {
            return getSerialCacheInfoFromFile();
        }
    });

    @Value("${serial.file.path}")
    private String serialFilePath;

    @Value("${serial.reload.threshold}")
    private int serialReloadThreshold;

    @Value("${serial.min.batch}")
    private int serialMinBatch;


    //每次最多获取个数
    @Value("${serial.max.batch}")
    private int serialMaxBatch;

    @Value("${serial.load.batch.size}")
    private int serialLoadBatchSize;

    @Autowired
    private SerialDAO serialDAO;



    public synchronized void getSerialBatchFromCache(Integer reqNum){

        LG.info("getSerialBatchFromCache, reqNum:" + reqNum);
//        if (null == reqNum || reqNum < serialMinBatch || reqNum > serialMaxBatch || reqNum % serialMinBatch != 0) {
//            reqNum = serialMinBatch;
//        }
        reqNum = serialMinBatch;

        //从缓存获取filep fileEnd dbStart dbEnd
        SerialCacheInfo serialCacheInfo;
        try {
            LG.info(">>>>");
            LG.info(">>>>222");
            serialCacheInfo = loadingCache.get(CACHE_NAME);
        } catch (ExecutionException e) {
            LG.info("serial cache error");
            throw new BusinessException(BusinessError.SERIAL_CACHE_ERROR.errorCode(),
                    BusinessError.SERIAL_CACHE_ERROR.errorMsg());
        }

        LG.info(">>>>" + serialCacheInfo.toString());

        //检查数据格式
        checkFileFormat(serialCacheInfo.getFilep(), serialCacheInfo.getFileEnd(), serialCacheInfo.getDbStart(), serialCacheInfo.getDbEnd());


        //当前数据不够(dbEnd <= fileEnd时，剩余数量小于阈值或filep加上本次的reqNum已大于fileEnd) 需要立即触发一次reload
        if (serialCacheInfo.getDbEnd() == serialCacheInfo.getFileEnd() && (serialCacheInfo.getFileEnd() - serialCacheInfo.getFilep() < serialReloadThreshold || (serialCacheInfo.getFilep() + reqNum) >= serialCacheInfo.getFileEnd())) {

            Serial serial = reloadFromDB();

            //更新的dbstart和dbend
            serialCacheInfo.setDbStart(serial.getSerial());
            serialCacheInfo.setDbEnd(serial.getSerial() + serialLoadBatchSize);
        }

        LG.info(">>>>55555");

        long startFilep = 0L;
        long endFilep = 0L;
        if((serialCacheInfo.getFilep() + reqNum) < serialCacheInfo.getFileEnd()){
            //当前段数据足够，取filep 和 filep + reqNum 之间
            startFilep = serialCacheInfo.getFilep();
            endFilep = serialCacheInfo.getFilep() + reqNum;
            serialCacheInfo.setFilep(endFilep);

        }else if((serialCacheInfo.getFilep() + reqNum) == serialCacheInfo.getFileEnd()){
            //当前段数据刚好足够本次取完，取filep 和 fileEnd(filep + reqNum)之间的序列,并更新filep和fileEnd
            startFilep = serialCacheInfo.getFilep();
            endFilep = serialCacheInfo.getFilep() + reqNum;

            serialCacheInfo.setFilep(serialCacheInfo.getDbStart());
            serialCacheInfo.setFileEnd(serialCacheInfo.getDbEnd());

        }else {
            //如果当前剩余少于reqNum ，则直接开始读取dbStart和dbEnd
            startFilep = serialCacheInfo.getDbStart();
            endFilep = startFilep + reqNum;
            serialCacheInfo.setFilep(endFilep);
            serialCacheInfo.setFileEnd(serialCacheInfo.getDbEnd());
        }

        //更新缓存
        loadingCache.put(CACHE_NAME,serialCacheInfo);

        //数据落盘
        ByteBuffer fileContent = transCache2Buffer(serialCacheInfo);
        try {
            SerialFileOperator.INSTANCE.writeFile(fileContent);
        } catch (Exception e) {
            loadingCache.invalidate(CACHE_NAME);
        }


        loadingCache.invalidate(CACHE_NAME);

        //注意，每次读取的500倍数的个数，是一个[0n,500n)的半开区间，故最大序列需要-1
        //return new GetSerialResponse(0, "ok", startFilep, endFilep);
        LG.info(">>>>startFilep:"+startFilep+",endFilep:"+endFilep);
    }

    private ByteBuffer transCache2Buffer(SerialCacheInfo serialCacheInfo) {

        ByteBuffer fileContent = ByteBuffer.allocate(BUFFSIZE);
        fileContent.putLong(serialCacheInfo.getFilep());
        fileContent.putLong(serialCacheInfo.getFileEnd());
        fileContent.putLong(serialCacheInfo.getDbStart());
        fileContent.putLong(serialCacheInfo.getDbEnd());
        return fileContent;
    }

    private ByteBuffer getSerialFileByteBuffer() {
        LG.info(">>>>>>44444");
        ByteBuffer fileContent = ByteBuffer.allocate(BUFFSIZE);
        int readIndex = SerialFileOperator.INSTANCE.readFile(fileContent);

        LG.info(">>>>>>44444,index:"+readIndex);
        if(readIndex == BUFFSIZE){
            //读到数据，需要翻转缓冲区
            //fileContent.flip();
        }else {
            //未读到数据，重新初始化
            Serial serial = reloadFromDB();

            LG.info(">>>>>>44444,serial:"+serial.getSerial());

            //赋值
            long dbStart = serial.getSerial();
            long dbEnd = serial.getSerial() + serialLoadBatchSize;
            long filep = dbStart;
            long fileEnd = dbEnd;

            //更新buffer中的filep、fileEnd、dbstart和dbend
            fileContent.clear();
            fileContent.putLong(0, filep);
            fileContent.putLong(COLUMNSIZE, fileEnd);
            fileContent.putLong(COLUMNSIZE * 2, dbStart);
            fileContent.putLong(COLUMNSIZE * 3, dbEnd);
        }
        return fileContent;
    }

    private SerialCacheInfo getSerialCacheInfoFromFile(){

        LG.info(">>>>>3333");
        //读取文件内容
        ByteBuffer fileContent = getSerialFileByteBuffer();

        LG.info(">>>>>3333+");
        fileContent.flip();
        LG.info(">>>>>3333++");

        long filep = fileContent.getLong();
        LG.info(">>>>>3333+++");
        long fileEnd = fileContent.getLong();
        long dbStart = fileContent.getLong();
        long dbEnd = fileContent.getLong();

        LG.info(">>>>filep:"+filep);
        LG.info(">>>>fileEnd:"+fileEnd);
        LG.info(">>>>dbStart:"+dbStart);
        LG.info(">>>>dbEnd:"+dbEnd);

        return new SerialCacheInfo(filep,fileEnd,dbStart,dbEnd);
    }


    public synchronized void getSerialBatch(Integer reqNum){

        LG.info("getSerialBatch, reqNum:" + reqNum);
        if (null == reqNum || reqNum < serialMinBatch || reqNum % serialMinBatch != 0) {
            reqNum = serialMinBatch;
        }

        //参数检查
        if (reqNum >= serialLoadBatchSize || reqNum + serialReloadThreshold >= serialLoadBatchSize) {
            //如果请求的序列数大于每次load db的序列数，或者请求数+阈值大于load db数
            LG.error("load db serials can not be less then reqNum or reqNum+serialReloadThreshold");
            throw new BusinessException(BusinessError.SERIAL_CONFIG_PARAM_INVALID.errorCode(),
                    BusinessError.SERIAL_CONFIG_PARAM_INVALID.errorMsg());
        }


        //读取文件内容
        ByteBuffer fileContent = ByteBuffer.allocate(BUFFSIZE);
        int readIndex = SerialFileOperator.INSTANCE.readFile(fileContent);

        long filep = 0L;
        long fileEnd = 0L;
        long dbStart = 0L;
        long dbEnd = 0L;

        if(readIndex != -1 && readIndex == BUFFSIZE){

            fileContent.flip();

            //获取文件中的数据
            filep = fileContent.getLong();
            fileEnd = fileContent.getLong();
            dbStart = fileContent.getLong();
            dbEnd = fileContent.getLong();
        }


        //检查数据格式(是否10位数字)
        boolean isFileOk = checkFileFormat(filep, fileEnd, dbStart, dbEnd);

        //当文件数据格式不正确，或当前数据不够(dbEnd <= fileEnd时，剩余数量小于阈值或filep加上本次的reqNum已大于fileEnd) 需要立即触发一次reload
        if (!isFileOk || (dbEnd <= fileEnd && (fileEnd - filep < serialReloadThreshold || (filep + reqNum) >= fileEnd))) {

            Serial serial = reloadFromDB();

            //更新文件和上下文中的dbstart和dbend
            fileContent.putLong(COLUMNSIZE * 2, serial.getSerial());
            fileContent.putLong(COLUMNSIZE * 3, serial.getSerial() + serialLoadBatchSize);
            dbStart = serial.getSerial();
            dbEnd = serial.getSerial() + serialLoadBatchSize;

            if(!isFileOk){
                //如果是文件格式的问题，还需初始化filep和fileEnd
                fileContent.putLong(0, dbStart);
                fileContent.putLong(COLUMNSIZE, dbEnd);
                filep = dbStart;
                fileEnd = dbEnd;
            }
        }

        //如果当前位置已到最后，更新文件和上下文中的filep和fileend
        if (filep == fileEnd) {
            fileContent.putLong(0, dbStart);
            fileContent.putLong(COLUMNSIZE, dbEnd);
            filep = dbStart;
            fileEnd = dbEnd;
        }


        //更新filep,如果filep+reqNum >= fileEnd，则更新filep=fileEnd
        long newFilep = (filep + reqNum)>=fileEnd?fileEnd:(filep + reqNum);
        fileContent.putLong(0, newFilep);

        // 数据落盘
        SerialFileOperator.INSTANCE.writeFile(fileContent);

        //注意，每次读取的500倍数的个数，是一个[0n,500n)的半开区间，故最大序列需要-1
        LG.info(">>>>>>filep:{},newFilep:{}",filep,newFilep-1);
    }

    private boolean checkFileFormat(long filep, long fileEnd, long dbStart, long dbEnd){

        if(filep < MIN_SERIAL || fileEnd < MIN_SERIAL || dbStart < MIN_SERIAL || dbEnd < MIN_SERIAL){
            return false;
        }
        return true;
    }

    /**
     * select for update 锁表获取新值并更新
     * @return
     */
    @Transactional
    private Serial reloadFromDB(){


        Serial serial = serialDAO.querySerialForUpdate();

        long num = serial.getSerial();
        long newNum = num+serialLoadBatchSize;

        serialDAO.updateSerial(newNum,serial.getId(),num);

        serial.setSerial(newNum);

        return serial;
    }
}
