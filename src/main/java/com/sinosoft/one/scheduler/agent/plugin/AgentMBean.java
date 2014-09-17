package com.sinosoft.one.scheduler.agent.plugin;


import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * agent暴露给server调用的Mbean接口
 * Created by bin on 14-1-17.
 */
public interface AgentMBean {

    /**
     * 获取任务信息
     * @return
     * @throws Exception
     */
    TabularData getAllJobDetails() throws Exception;

    /**
     * 获取trigger信息
     * @return
     * @throws Exception
     */
    List<CompositeData> getAllTriggers() throws Exception;


    /**
     * 增加和修改计划
     *
     * @param jobName
     * @param jobGroup
     * @param abstractTriggerInfo
     * @throws Exception
     */
    void scheduleJob(String jobName, String jobGroup,
                     Map<String, Object> abstractTriggerInfo) throws Exception;


    /**
     * 删除计划
     * @param triggerName
     * @param triggerGroup
     * @return
     * @throws Exception
     */
    boolean unscheduleJob(String triggerName, String triggerGroup) throws Exception;

    /**
     * 停止计划(本次会执行完毕,从下次开始停止)
     * @param triggerName
     * @param triggerGroup
     * @throws Exception
     */
    void pauseTrigger(String triggerName, String triggerGroup) throws Exception;

    /**
     * 开始计划
     * @param triggerName
     * @param triggerGroup
     * @throws Exception
     */
    void resumeTrigger(String triggerName, String triggerGroup) throws Exception;

    /**
     * 立即执行一次
     * @param jobName
     * @param jobGroup
     * @param jobDataMap
     * @throws Exception
     */
    void triggerJob(String jobName, String jobGroup,
                    Map<String, String> jobDataMap) throws Exception;
}
