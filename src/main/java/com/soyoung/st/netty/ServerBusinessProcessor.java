package com.soyoung.st.netty;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

public class ServerBusinessProcessor extends AppBusinessProcessor {

    @Override
    public void process(NettyMessage message) {
        logger.info("服务端执行业务处理...");
        String req = message.bodyToString();
        // TODO: biz goes here

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(req.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        List<String> lines = Lists.newArrayList();
        try{

            while ( (line = br.readLine()) != null ) {
                if (!line.trim().equals("")) {
                    lines.add(line);
                }
            }
        }catch (IOException e){

        }
        NettyServerProxy.INSTANCE.putResponse(message);

        String rsp = req + " ---> 服务响应";
        message.setMessageBody(rsp);
    }
}
