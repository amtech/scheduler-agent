package org.one.scheduler.agent.core;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.util.List;
import java.util.Properties;

/**
 * Created by bin on 14-5-22.
 */
public class TriggerFileTest {

    TriggerFile triggerFile;

    @Before
    public void before(){
        this.triggerFile = new TriggerFile();
        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.initProperties(new Properties());
    }

    @Test
    public void deleteTest(){
        triggerFile.delete();
    }

    @Test
    public void writeTest(){
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setName("t1");
        cronTrigger.setJobName("a1");
        try {
            cronTrigger.setCronExpression("0 0 10,14,16 * * ?");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        triggerFile.write(cronTrigger);
    }

    @Test
    public void readTest(){
        List<CronTriggerImpl> cronTriggerList = triggerFile.read();
        Assert.assertNotNull(cronTriggerList);
    }
}
