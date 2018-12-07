package com.soyoung.st.netty;

public class ServerBusinessProcessor extends AppBusinessProcessor {

    @Override
    public void process(NettyMessage message) {
        logger.info("服务端执行业务处理...");
        String req = message.bodyToString();
        // TODO: biz goes here
        String rsp = req + " ---> 服务响应";
        message.setMessageBody(rsp);
    }
}
