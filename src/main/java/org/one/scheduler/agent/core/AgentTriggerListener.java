package org.one.scheduler.agent.core;

import com.alibaba.fastjson.JSONObject;
import org.one.scheduler.agent.dto.Log;
import org.one.scheduler.agent.utils.Connector;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bin on 14-5-15.
 */
public class AgentTriggerListener implements TriggerListener {
    @Override
    public String getName() {
        return AgentConstant.APP_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = simpleDateFormat.format(new Date());
        Log log = new Log();
        log.setAppName(AgentConstant.APP_NAME);
        log.setJobName(trigger.getJobKey().getName());
        log.setTriggerName(trigger.getKey().getName());
        log.setStartTime(startTime);
        String result = Connector.sendStartLog(log);
        if(result!=null){
            JSONObject jsonObject = JSONObject.parseObject(result);
            String id = jsonObject.getString("id");
            context.put("id",id);
        }
        context.put("startTime",startTime);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        boolean flag = (Boolean)context.get("flag");
        if(flag){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String endTime = simpleDateFormat.format(new Date());
            Log log = new Log();
            log.setAppName(AgentConstant.APP_NAME);
            log.setJobName(trigger.getJobKey().getName());
            log.setTriggerName(trigger.getKey().getName());
            log.setStartTime((String)context.get("startTime"));
            log.setEndTime(endTime);
            log.setId((String)context.get("id"));
            String result = Connector.sendEndLog(log);
            if(result == null){
                parseToJson(log);
            }else{
                JSONObject jsonObject = JSONObject.parseObject(result);
                String statusCode = jsonObject.getString("statusCode");
                if(statusCode.equals("500")) {
                    parseToJson(log);
                }
            }
        }

    }

    private void parseToJson(Log log) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appName",log.getAppName());
        jsonObject.put("jobName",log.getJobName());
        jsonObject.put("triggerName",log.getTriggerName());
        jsonObject.put("startTime",log.getStartTime());
        jsonObject.put("endTime",log.getEndTime());
        TriggerLogManager.concurrentLinkedQueue.add(jsonObject.toJSONString());
    }
}
