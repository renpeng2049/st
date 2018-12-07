package com.soyoung.st.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AppBusinessProcessor {

    protected static Logger logger = LoggerFactory.getLogger(AppBusinessProcessor.class);

    /**
     * 执行业务处理，参数是包含的是请求消息，处理完毕后，将响应消息设置到message对象中
     *
     * @param message
     *            请求/响应消息载体
     */
    public abstract void process(NettyMessage message);
}
