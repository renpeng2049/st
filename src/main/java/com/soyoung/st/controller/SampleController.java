package com.soyoung.st.controller;

import com.soyoung.st.common.EsClient;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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
}
