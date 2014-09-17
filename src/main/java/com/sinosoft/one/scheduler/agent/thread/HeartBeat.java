package com.sinosoft.one.scheduler.agent.thread;

import com.sinosoft.one.scheduler.agent.core.AgentConstant;
import com.sinosoft.one.scheduler.agent.utils.Connector;

/**
 * Created by bin on 14-5-15.
 */
public class HeartBeat implements Runnable{

    @Override
    public void run() {
        Connector.HeartBeat(AgentConstant.APP_NAME);
    }
}
