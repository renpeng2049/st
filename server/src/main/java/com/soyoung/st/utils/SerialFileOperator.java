package com.soyoung.st.utils;

import com.soyoung.st.exception.BusinessError;
import com.soyoung.st.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public enum SerialFileOperator {

    INSTANCE
    ;

    private final Logger LG = LoggerFactory.getLogger("SerialFileProxy");

    private FileChannel readFileChannel;
    private FileChannel writeFileChannel;

    private SerialFileOperator(){

        //检查文件是否存在，不存在则创建
        File file = new File("C:\\Users\\cooperren\\Desktop\\root");
        if (!file.exists()){
            LG.warn("serialFile: doesn't exist.");
            try {
                boolean created = file.createNewFile();
                if(!created){
                    LG.error("can not create serial file");
                    throw new BusinessException(BusinessError.CREATE_SERIAL_FILE_FAILED.errorCode(),
                            BusinessError.CREATE_SERIAL_FILE_FAILED.errorMsg());
                }
            } catch (IOException e) {
                LG.error("create serial file error:",e);
                throw new BusinessException(BusinessError.CREATE_SERIAL_FILE_FAILED.errorCode(),
                        BusinessError.CREATE_SERIAL_FILE_FAILED.errorMsg());
            }
        }

        //构建读写文件channel
        try {
            RandomAccessFile rafReader = new RandomAccessFile(file, "rw");
            RandomAccessFile rafWriter = new RandomAccessFile(file, "rw");
            readFileChannel = rafReader.getChannel();
            writeFileChannel = rafWriter.getChannel();

        } catch (FileNotFoundException e) {
            LG.error("create serial file channel error:",e);
            throw new BusinessException(BusinessError.SERIAL_FILE_DOESNT_EXIST.errorCode(),
                    BusinessError.SERIAL_FILE_DOESNT_EXIST.errorMsg());
        }
    }

    public int readFile(ByteBuffer buffer){
        int readIndex = -1;
        try {
            readIndex = readFileChannel.read(buffer,0);
        } catch (IOException e) {
            LG.error("read serial file error:",e);
            throw new BusinessException(BusinessError.LOAD_SERIAL_FILE_FAILED.errorCode(),
                    BusinessError.LOAD_SERIAL_FILE_FAILED.errorMsg());
        }
        return readIndex;
    }

    public synchronized void writeFile(ByteBuffer buffer){
        buffer.flip();
        try {
            writeFileChannel.write(buffer,0);
            writeFileChannel.force(true);
        } catch (IOException e) {
            LG.error("write serial file error:",e);
            throw new BusinessException(BusinessError.LOAD_SERIAL_FILE_FAILED.errorCode(),
                    BusinessError.LOAD_SERIAL_FILE_FAILED.errorMsg());
        }
    }

    public void close(){

        try {
            readFileChannel.close();
            writeFileChannel.close();
        } catch (IOException e) {
            LG.error("close serial file channel error:",e);
            throw new BusinessException(BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorCode(),
                    BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorMsg());
        }finally {
            if(null != readFileChannel){
                try {
                    readFileChannel.close();
                } catch (IOException e) {
                    LG.error("close serial file read channel error:",e);
                    throw new BusinessException(BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorCode(),
                            BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorMsg());
                }
            }

            if(null != writeFileChannel){
                try {
                    writeFileChannel.close();
                } catch (IOException e) {
                    LG.error("close serial file write channel error:",e);
                    throw new BusinessException(BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorCode(),
                            BusinessError.CLOSE_SERIAL_FILE_RESOURCE_ERROR.errorMsg());
                }
            }
        }
        LG.info("close serial file op");
    }
}
