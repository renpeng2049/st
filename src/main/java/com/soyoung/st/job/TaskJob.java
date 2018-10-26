package com.soyoung.st.job;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.soyoung.st.model.SampleInfo;
import com.soyoung.st.service.SampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TaskJob implements SimpleJob {

    private Logger logger = LoggerFactory.getLogger(TaskJob.class);

    @Autowired
    private SampleService sampleService;

    @Override
    public void execute(ShardingContext shardingContext) {

        Integer shardingTotalConunt = shardingContext.getShardingTotalCount();
        Integer shardingItem = shardingContext.getShardingItem();
        logger.info("sharding total count:{}",shardingTotalConunt);
        logger.info("sharding item:{}",shardingItem);

        List<SampleInfo> sampleInfos = sampleService.querySampleList(shardingTotalConunt,shardingItem);
        logger.info("query sample list:{}", JSON.toJSONString(sampleInfos));
    }
}
