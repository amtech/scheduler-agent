package org.one.scheduler.agent.core;

import org.quartz.Scheduler;
import org.quartz.utils.PropertiesParser;

/**
 * Created by bin on 14-5-15.
 * trigger抽象类,根据properties中配置不同创建不同的trigger,提供统一的trigger方法,做到trigger的统一性
 */
public abstract class AbstractTrigger {

    /**
     * 根据properties中配置不同创建不同的trigger
     * @return
     */
    public static AbstractTrigger build(){
        AbstractTrigger abstractTrigger = null;
        if(AgentConstant.SERVER_IP != null && AgentConstant.SERVER_PORT != null){
            abstractTrigger = new RemoteTrigger();
        }else{
            abstractTrigger = new LocalTrigger();
        }
        return abstractTrigger;
    }

    /**
     * 初始化trigger
     * @param parser
     * @param scheduler
     */
    public void trigger(PropertiesParser parser,Scheduler scheduler){}
}
