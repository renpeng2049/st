package com.soyoung.st.common;

import com.google.common.collect.Lists;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public enum EsClient {
    INSTANCE;


    private TransportClient client;

    private EsClient(){


        TransportClient transportClient = null;
        try {

            Settings settings = Settings.builder().put("cluster.name", "fit").put("client.transport.sniff", true).build();
            transportClient = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("9.77.8.77"), 9300));
        } catch (UnknownHostException e) {
            throw new IllegalStateException("实例化es client 失败,"+ e.getMessage());
        }

        client = transportClient;
    }

    public TransportClient getClient(){
        return client;
    }

    public RestHighLevelClient buildRestHighLevelClient(){

        //获取es主机列表
        List<HttpHost> hostList = Lists.newArrayList();

        String ipports = EsConfig.getIpports();
        String[] arr = ipports.split(";");
        for(String ipport : arr){
            String[] tmp = ipport.split(":");
            if(tmp.length != 2){
                throw new IllegalStateException("elasticsearch  主机配置错误, "+ipport);
            }
            HttpHost hh = new HttpHost(tmp[0],Integer.parseInt(tmp[1]),"http");
            hostList.add(hh);
        }

        HttpHost[] hhArr = new HttpHost[hostList.size()];
        hostList.toArray(hhArr);
        return new RestHighLevelClient(RestClient.builder(hhArr).build());
    }
}
