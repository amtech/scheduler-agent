package com.sinosoft.one.scheduler.agent.core;

import com.sinosoft.one.scheduler.agent.utils.Connector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by bin on 14-5-15.
 * agent初始化入口
 */
public class AgentListener implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.start();


    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Connector.getHttpClient().getConnectionManager().shutdown();
    }
}
