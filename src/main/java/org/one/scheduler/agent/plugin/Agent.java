package org.one.scheduler.agent.plugin;

import org.one.scheduler.agent.core.AgentTrigger;
import org.one.scheduler.agent.core.TriggerFile;
import org.quartz.*;
import org.quartz.core.jmx.JobDetailSupport;
import org.quartz.core.jmx.TriggerSupport;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

/**
 * Created by bin on 14-1-17.
 */
public class Agent implements AgentMBean {

    private final Scheduler scheduler;
    private AgentTrigger agentTrigger;
    private TriggerFile triggerFile;

    public Agent(Scheduler scheduler) {
        this.scheduler = scheduler;
        agentTrigger = new AgentTrigger();
        triggerFile = new TriggerFile();
    }

    @Override
    public TabularData getAllJobDetails() throws Exception {
        try {
            List<JobDetail> detailList = new ArrayList<JobDetail>();
            for (String jobGroupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName))) {
                    detailList.add(scheduler.getJobDetail(jobKey));
                }
            }
            return JobDetailSupport.toTabularData(detailList.toArray(new JobDetail[detailList.size()]));
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    @Override
    public List<CompositeData> getAllTriggers() throws Exception {
        try {
            List<Trigger> triggerList = new ArrayList<Trigger>();
            for (String triggerGroupName : scheduler.getTriggerGroupNames()) {
                for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName))) {
                    triggerList.add(scheduler.getTrigger(triggerKey));
                }
            }
            return TriggerSupport.toCompositeList(triggerList);
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    @Override
    public void scheduleJob(String jobName, String jobGroup,
                            Map<String, Object> abstractTriggerInfo) throws Exception {
        unscheduleJob((String) abstractTriggerInfo.get("name"),"DEFAULT");
        //创建remoteTrigger临时文件
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setCronExpression((CronExpression)abstractTriggerInfo.get("CronExpression"));
        cronTrigger.setName((String) abstractTriggerInfo.get("name"));
        cronTrigger.setJobName(jobName);
        cronTrigger.setJobGroup("DEFAULT");
        triggerFile.write(cronTrigger);

        //初始化trigger
        agentTrigger.createTrigger(jobName,jobGroup,abstractTriggerInfo,scheduler);

    }

    @Override
    public boolean unscheduleJob(String triggerName, String triggerGroup) throws Exception {
        triggerFile.delete(triggerName);
        try {
            return scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    @Override
    public void pauseTrigger(String triggerName, String triggerGroup) throws Exception {
        try {
            scheduler.pauseTrigger(triggerKey(triggerName, triggerGroup));
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    @Override
    public void resumeTrigger(String triggerName, String triggerGroup) throws Exception {
        try {
            scheduler.resumeTrigger(triggerKey(triggerName, triggerGroup));
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }

    @Override
    public void triggerJob(String jobName, String jobGroup, Map<String, String> jobDataMap) throws Exception {
        try {
            scheduler.triggerJob(jobKey(jobName, jobGroup),null);
        } catch (Exception e) {
            throw newPlainException(e);
        }
    }


    private Exception newPlainException(Exception e) {
        String type = e.getClass().getName();
        if(type.startsWith("java.") || type.startsWith("javax.")) {
            return e;
        } else {
            Exception result = new Exception(e.getMessage());
            result.setStackTrace(e.getStackTrace());
            return result;
        }
    }
}
