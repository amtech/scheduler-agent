package org.one.scheduler.agent.core;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;

import java.util.List;
import java.util.Properties;

/**
 * Created by bin on 14-5-22.
 */
public class TaskSchedulerTest {

    TaskScheduler taskScheduler;
    List<JobDetail> jobDetails;
    Properties properties;
    @Before
    public void before(){
        taskScheduler = new TaskScheduler();
        properties = new Properties();
        //taskScheduler.initProperties(properties);
    }

    @Test
    public void initPropertiesTest(){
        taskScheduler.initProperties(properties);
    }

    @Test
    public void createJobDetailsTest(){
        taskScheduler.createJobDetails();
    }

    @Test
    public void startTest(){
        taskScheduler.start();
    }

    public static void main(String[] args){
       new TaskScheduler().start();
    }
}
