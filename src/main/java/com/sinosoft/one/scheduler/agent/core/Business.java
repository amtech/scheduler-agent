package com.sinosoft.one.scheduler.agent.core;

/**
 * 任务执行接口,提供给外部程序编写业务逻辑
 */
public interface Business {
    public void execute() throws Exception;
}
