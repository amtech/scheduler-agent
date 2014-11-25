package org.one.scheduler.agent.core;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 此类用来对任务的trigger触发时实例化job对象，里面给Task对象中的business属性赋值
 * Created by bin on 14-6-5.
 */
public class AgentJobFactory implements JobFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String,Business> businessMap = new HashMap<String, Business>();

    public Map<String, Business> getBusinessMap() {
        return businessMap;
    }


    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            if(log.isDebugEnabled()) {
                log.debug(
                        "Producing instance of Job '" + jobDetail.getKey() +
                                "', class=" + jobClass.getName());
            }
            Task task = null;
            for(String jobName:businessMap.keySet()){
                if(jobName.equals(bundle.getJobDetail().getKey().getName())){
                    task = (Task) jobClass.newInstance();
                    task.setBusiness(businessMap.get(jobName));
                    break;
                }
            }
            return task;
        } catch (Exception e) {
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
            throw se;
        }
    }
}
