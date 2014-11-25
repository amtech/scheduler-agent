package org.one.scheduler.agent;

import org.one.scheduler.agent.core.Business;

import java.io.IOException;

/**
 * Created by bin on 14-5-22.
 */
public class A implements Business {
    @Override
    public void execute() throws Exception {
        System.out.println("-------");
        throw new IOException("异常测试");
    }
}
