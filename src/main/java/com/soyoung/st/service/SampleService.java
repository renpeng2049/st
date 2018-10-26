package com.soyoung.st.service;

import com.google.common.collect.Maps;
import com.soyoung.st.dao.SampleDao;
import com.soyoung.st.model.SampleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SampleService {


    @Autowired
    private SampleDao sampleDao;

    public List<SampleInfo> querySampleList(Integer shardingTotal, Integer shardingItem){

        Map<String,Object> param = Maps.newHashMap();
        param.put("shardingTotal",shardingTotal);
        param.put("shardingItem",shardingItem);
        return sampleDao.querySimpleList(param);
    }
}
