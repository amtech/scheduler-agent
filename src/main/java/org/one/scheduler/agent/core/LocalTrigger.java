package org.one.scheduler.agent.core;

import org.one.scheduler.agent.exception.AgentException;
import org.quartz.CronExpression;
import org.quartz.Scheduler;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.utils.PropertiesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;

/**
 * Created by bin on 14-5-15.
 * 本地trigger,负责创建本地trigger并初始化
 */
public class LocalTrigger extends AbstractTrigger {


    private final Logger logger = LoggerFactory.getLogger(getClass());
    private TriggerFile triggerFile;
    private AgentTrigger agentTrigger;
    public LocalTrigger(){
        triggerFile = new TriggerFile();
        agentTrigger = new AgentTrigger();
    }

    @Override
    public void trigger(PropertiesParser parser,Scheduler scheduler){
        //1.删除本地trigger临时文件
        triggerFile.delete();
        //2.根据properties中配置的trigger初始化trigger
        String[] jobName = parser.getStringArrayProperty("jobName");
        for(int i=0;i<jobName.length;i++){
            String[] triggerName = parser.getStringArrayProperty(jobName[i]+".triggerName");
            if(triggerName==null){
                AgentException agentException = new AgentException("triggerName配置不正确!");
                throw  agentException;
            }
            for(int j=0;j<triggerName.length;j++){
                String trigger = parser.getStringProperty(triggerName[j]+".trigger",null);
                if(trigger==null){
                    AgentException agentException = new AgentException(triggerName[j]+"的trigger配置不正确!");
                    logger.error(agentException.getMessage());
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
                    AgentException agentException = new AgentException("创建cronExpression失败,请正确填写cronExpression");
                    logger.error(agentException.getMessage());
                    throw agentException;
                }
                    //初始化tirgger
                try {
                    agentTrigger.createTrigger(jobName[i],"DEFAULT",triggerMap,scheduler);
                } catch (Exception e) {
                    AgentException agentException = new AgentException("初始化trigger失败");
                    logger.error(agentException.getMessage());
                    throw agentException;
                }
                CronTriggerImpl cronTrigger = new CronTriggerImpl();
                cronTrigger.setCronExpression(cronExpression);
                cronTrigger.setJobName(jobName[i]);
                //cronTrigger.setJobGroup("DEFAULT");
                cronTrigger.setName(triggerName[j]);
                //3.创建trigger临时文件
                triggerFile.write(cronTrigger);
            }
        }
    }
}
