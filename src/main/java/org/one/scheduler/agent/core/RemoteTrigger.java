package org.one.scheduler.agent.core;

import org.one.scheduler.agent.exception.AgentException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.utils.PropertiesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by bin on 14-5-15.
 * 远程trigger,负责创建远程trigger并初始化
 */
public class RemoteTrigger extends AbstractTrigger {

    private TriggerFile triggerFile;
    private List<CronTriggerImpl> cronTriggerList;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RemoteTrigger(){
        triggerFile = new TriggerFile();
    }

    @Override
    public void trigger(PropertiesParser parser,Scheduler scheduler) {
            String path = System.getProperty("user.home");
            String separator = System.getProperty("file.separator");
            File remoteTrigger = new File(path+separator+"trigger"+separator+"remote");
            remoteTrigger.mkdirs();
            String[] fileList = remoteTrigger.list();
            //2.判断是否存在remoteTrigger临时文件
            if(fileList.length==0){
                //3.判断此次是否可连接至server,不可连直接报异常,可连时提供空跑的job
                if(AgentConstant.CONNECT==false){
                    AgentException agentException = new AgentException("请检查配置文件中server信息配置");
                    logger.error(agentException.getMessage());
                    throw agentException;
                }
            }else{
                cronTriggerList = triggerFile.read();
                for(CronTriggerImpl cronTrigger:cronTriggerList){
                    try {
                        scheduler.scheduleJob(cronTrigger);
                    } catch (SchedulerException e) {
                        AgentException agentException = new AgentException(e.getMessage());
                        logger.error(agentException.getMessage());
                        throw agentException;
                    }
                }
            }
        }

    }


