package com.sinosoft.one.scheduler.agent.core;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 对job接口的封装
 * Created by bin on 14-1-2.
 */
  public class Task implements Job{

    private static Logger logger = Logger.getLogger(Task.class);

    public void setBusiness(Business business) {
        this.business = business;
    }

    private  Business business;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try{
            business.execute();
        }catch (Throwable t){
            logger.error(t.getMessage());
           throw new JobExecutionException(t);
        }
    }

}
