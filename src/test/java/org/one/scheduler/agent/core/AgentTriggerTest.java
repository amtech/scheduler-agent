package org.one.scheduler.agent.core;

import org.one.scheduler.agent.exception.AgentException;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by bin on 14-5-23.
 */
public class AgentTriggerTest {

    TaskScheduler taskScheduler;
    Properties properties;
    Scheduler scheduler;
    List<JobDetail> jobDetails;
    AgentTrigger agentTrigger;

    @Before
    public void before(){
        taskScheduler = new TaskScheduler();
        properties = new Properties();
        taskScheduler.initProperties(properties);
        StdSchedulerFactory factory = null;
        try {
            factory = new StdSchedulerFactory("quartz.properties");
            scheduler = factory.getScheduler();
        } catch (SchedulerException e) {
           e.printStackTrace();
        }
        jobDetails = taskScheduler.createJobDetails();
        for(JobDetail jobDetail:jobDetails){
            try {
                scheduler.addJob(jobDetail,false);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        agentTrigger = new AgentTrigger();
    }

    @Test
    public void createTriggerTest(){
        String[] jobName = taskScheduler.getCfg().getStringArrayProperty("jobName");
        for(int i=0;i<jobName.length;i++){
            String[] triggerName = taskScheduler.getCfg().getStringArrayProperty(jobName[i]+".triggerName");
            if(triggerName==null){
                AgentException agentException = new AgentException("triggerName配置不正确!");
                throw  agentException;
            }
            for(int j=0;j<triggerName.length;j++){
                String trigger = taskScheduler.getCfg().getStringProperty(triggerName[j]+".trigger",null);
                if(trigger==null){
                    AgentException agentException = new AgentException(triggerName[j]+"的trigger配置不正确!");
                    throw  agentException;
                }
                Map<String,Object> triggerMap = new HashMap<String, Object>();
                triggerMap.put("triggerClass","org.quartz.impl.triggers.CronTriggerImpl");
                triggerMap.put("name",triggerName[j]);
                triggerMap.put("jobName",jobName[i]);
                CronExpression cronExpression = null;
                try {
                    cronExpression = new CronExpression(trigger);
                    triggerMap.put("CronExpression",cronExpression);
                } catch (ParseException e) {
                   e.printStackTrace();
                }
                try {
                    agentTrigger.createTrigger(jobName[i], "DEFAULT", triggerMap, scheduler);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
