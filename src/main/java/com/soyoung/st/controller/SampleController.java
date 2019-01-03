package com.soyoung.st.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.soyoung.st.common.EsClient;
import com.soyoung.st.netty.ChannelOsRsp;
import com.soyoung.st.netty.NettyMessage;
import com.soyoung.st.netty.NettyServerProxy;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@RestController
public class SampleController {

    @RequestMapping("/hello")
    public String hello(){
        System.out.println("hello");
        return "ok";
    }


    @RequestMapping("/query")
    public String query() throws Exception {

        TransportClient client = EsClient.INSTANCE.getClient();

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        bulkRequestBuilder.add(client.prepareIndex("twitter", "tweet", "3").setSource(jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject()
        ));

        bulkRequestBuilder.add(client.prepareIndex("twitter", "tweet", "4")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "another post")
                        .endObject()
                )
        );


        BulkResponse bulkResponse = bulkRequestBuilder.get();
        if(bulkResponse.hasFailures()){
            return "fail";
        }

        return "ok";
    }

    @RequestMapping("/sendMsg")
    public String sendMsg(String msg,String ip) throws Exception {

        Map<String,String> msgMap = Maps.newHashMap();
        msgMap.put("command",msg);

        Map<String,Object> map = Maps.newHashMap();
        map.put("type","exec");
        map.put("body",msgMap);

        NettyMessage bizMsg = new NettyMessage(JSON.toJSONString(map));
        ChannelOsRsp cor = NettyServerProxy.INSTANCE.sendMsg(bizMsg,ip,10000L);



        return JSON.toJSONString(cor);
    }
}
