package org.one.scheduler.agent.thread;

import com.alibaba.fastjson.JSONObject;
import org.one.scheduler.agent.core.TriggerLogManager;
import org.one.scheduler.agent.utils.Connector;

/**
 * Created by bin on 14-5-15.
 */
public class SendLogCacheTask implements Runnable {
    @Override
    public void run() {
        String path = TriggerLogManager.mkdirs();
        String fileName = TriggerLogManager.read(path);
        if(fileName!=null){
            String result = Connector.sendLog(fileName);
            if(result!=null){
                JSONObject jsonObject = JSONObject.parseObject(result);
                String statusCode = jsonObject.getString("statusCode");
                if(statusCode=="200"){
                    TriggerLogManager.delete(fileName);
                }
            }
        }

    }
}
