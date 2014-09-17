package com.sinosoft.one.scheduler.agent.utils;

import com.sinosoft.one.scheduler.agent.core.TaskScheduler;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by bin on 14-6-17.
 */
public class ConnectorTest {

    @Test
    public void sendLogTest(){

        Connector.sendLog("/Users/bin/log/app1/app1.zip");
    }
}
