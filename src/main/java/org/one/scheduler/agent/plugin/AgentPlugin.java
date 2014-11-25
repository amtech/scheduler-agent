package org.one.scheduler.agent.plugin;

import org.one.scheduler.agent.core.AgentConstant;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by bin on 14-6-4.
 */
public class AgentPlugin implements SchedulerPlugin {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {

        if(!AgentConstant.APP_NAME.equals("")&&!AgentConstant.APP_PORT.equals("")){
            //rmi调用注册端口
            try {
                LocateRegistry.createRegistry(Integer.parseInt(AgentConstant.APP_PORT));
            } catch (RemoteException e) {
                logger.error(e.getMessage());
            }
            // 创建MBeanServer
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            // 新建MBean ObjectName, 在MBeanServer里标识注册的MBean
            ObjectName objectName = null;
            try {
                objectName = new ObjectName(QuartzSchedulerResources.generateJMXObjectName("QuartzScheduler", "core"));
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }

            // 创建MBean
            AgentMBean agentMBean = new Agent(scheduler);
            try {
                JMXConnectorServer cserver = JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL("service:jmx:rmi://"+AgentConstant.APP_IP+":"+AgentConstant.APP_PORT+"/"+"jndi/rmi://"+AgentConstant.APP_IP+":"+AgentConstant.APP_PORT+"/"+AgentConstant.APP_NAME),null,mbs);
                cserver.start();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            // 在MBeanServer里注册MBean, 标识为ObjectName(com.tenpay.jmx:type=Echo)
            try {
                mbs.registerMBean(agentMBean, objectName);
            } catch (InstanceAlreadyExistsException e) {
                logger.error(e.getMessage());
            } catch (MBeanRegistrationException e) {
                logger.error(e.getMessage());
            } catch (NotCompliantMBeanException e) {
                logger.error(e.getMessage());
            }
        }

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }



}
