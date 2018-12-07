package com.soyoung.st.netty;

public class ClientBusinessProcessor extends AppBusinessProcessor {

    public void process(NettyMessage message) {

        logger.info("客户端执行业务处理..., message:{}",message.bodyToString());
    }
}
