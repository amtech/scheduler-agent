package com.sinosoft.one.scheduler.agent.core;

import com.alibaba.fastjson.JSONObject;
import com.sinosoft.one.scheduler.agent.dto.Log;
import com.sinosoft.one.scheduler.agent.utils.Connector;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bin on 14-5-15.
 */
public class AgentJobListener implements JobListener{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String getName() {
        return AgentConstant.APP_NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        context.put("flag",true);
        if(jobException!=null){
            context.put("flag",false);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String endTime = simpleDateFormat.format(new Date());
            Log log = new Log();
            log.setAppName(AgentConstant.APP_NAME);
            log.setJobName(context.getJobDetail().getKey().getName());
            log.setTriggerName(context.getTrigger().getKey().getName());
            log.setStartTime((String)context.get("startTime"));
            log.setEndTime(endTime);
            log.setId((String)context.get("id"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jobException.printStackTrace(new PrintStream(baos));
            try {
                baos.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            log.setExceptionMsg(baos.toString());
            String result = Connector.sendEndLog(log);
            if(result == null){
                parseToJson(log);
            }else{
                JSONObject jsonObject = JSONObject.parseObject(result);
                String statusCode = jsonObject.getString("statusCode");
                if(statusCode.equals("500")){
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
        jsonObject.put("beginTime",log.getStartTime());
        jsonObject.put("endTime",log.getEndTime());
        jsonObject.put("exceptionMsg",log.getExceptionMsg());
        TriggerLogManager.concurrentLinkedQueue.add(jsonObject.toJSONString());
    }
}
