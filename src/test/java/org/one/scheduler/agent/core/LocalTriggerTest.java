package org.one.scheduler.agent.core;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;

import java.util.List;
import java.util.Properties;

/**
 * Created by bin on 14-5-22.
 */
public class LocalTriggerTest {
    TaskScheduler taskScheduler;
    List<JobDetail> jobDetails;
    Properties properties;
    LocalTrigger localTrigger;
    @Before
    public void before(){
        taskScheduler = new TaskScheduler();
        properties = new Properties();
        taskScheduler.initProperties(properties);
        taskScheduler.createJobDetails();
        localTrigger = new LocalTrigger();
    }

    @Test
    public void triggerTest(){
        localTrigger.trigger(taskScheduler.getCfg(),taskScheduler.getScheduler());
    }
}
