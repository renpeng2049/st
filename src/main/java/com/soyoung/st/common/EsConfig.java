package com.soyoung.st.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EsConfig {

    private static String ipports;

    public static String getIpports(){

        return ipports;
    }

    @Value("${elasticsearch.ipports}")
    public void setConfig(String ipports){

        EsConfig.ipports = ipports;
    }
}
