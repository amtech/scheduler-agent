package com.sinosoft.one.scheduler.agent.thread;

import com.sinosoft.one.scheduler.agent.core.TriggerLogManager;

/**
 * Created by bin on 14-6-9.
 */
public class Consumer implements Runnable {

    @Override
    public void run() {
        TriggerLogManager.write();
    }
}
