package com.soyoung.st.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

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

        JSONObject json = JSON.parseObject(req);
        String errcode = json.getString("errcode");
        String errmsg = json.getString("errmsg");
        String stdout = json.getString("standard_output");
        String errout = json.getString("error_output");

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stdout.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String line;
        List<String> lines = Lists.newArrayList();
        try{

            while ( (line = br.readLine()) != null ) {
                if (!line.trim().equals("")) {
                    lines.add(line);
                }
            }
        }catch (IOException e){
            logger.error("解析报文异常:",e);
        }

        logger.info(">>>>>list:{}",lines);
        String resultCode = lines.get(lines.size()-1);
        String newStdout = "";
        if(lines.size()>1){
            lines.remove(lines.size()-1);
            newStdout = StringUtils.join(lines,"\n");
        }

        ChannelOsRsp cor = new ChannelOsRsp();
        cor.setErrcode(errcode);
        cor.setErrmsg(errmsg);
        cor.setResult_code(resultCode);
        cor.setError_output(errout);
        cor.setStandard_output(newStdout);
        NettyServerProxy.INSTANCE.putResponse(cor,message.getLogId());

        String rsp = req + " ---> 服务响应";
        message.setMessageBody(rsp);
    }
}
