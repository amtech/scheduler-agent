package org.one.scheduler.agent.plugin;


import org.junit.Assert;
import org.junit.Test;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by bin on 14-6-12.
 */
public class AgentPluginTest {

    @Test
    public void MBeanTest(){
        try {
            ObjectName objectName = new ObjectName("quartz:type=QuartzScheduler,name=QuartzScheduler,instance=core");
            JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:8801/app1");
            JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            Assert.assertNotNull(connection.getAttribute(objectName, "AllJobDetails"));
            // ;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (MBeanException e) {
            e.printStackTrace();
        } catch (ReflectionException e) {
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
}
