package org.one.scheduler.agent.core;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by bin on 14-5-23.
 */
public class TriggerLogManagerTest {

    @Before
    public void before(){
        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.start();
    }

    @Test
    public void writeTest(){

    }

    @Test
    public void readTest(){
       String path = TriggerLogManager.mkdirs();
       TriggerLogManager.read(path);
    }


}
