package com.sinosoft.one.scheduler.agent.core;

import com.alibaba.fastjson.JSONObject;
import com.sinosoft.one.scheduler.agent.dto.Log;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bin on 14-5-23.
 */
public class TriggerLogManagerTest {

    @Before
    public void before(){
        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.start();
    }

    @Test
    public void writeTest(){

    }

    @Test
    public void readTest(){
       String path = TriggerLogManager.mkdirs();
       TriggerLogManager.read(path);
    }


}
