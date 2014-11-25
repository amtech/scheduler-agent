package org.one.scheduler.agent.thread;

import org.one.scheduler.agent.core.AgentConstant;
import org.one.scheduler.agent.utils.Connector;

/**
 * Created by bin on 14-5-15.
 */
public class HeartBeat implements Runnable{

    @Override
    public void run() {
        Connector.HeartBeat(AgentConstant.APP_NAME);
    }
}
