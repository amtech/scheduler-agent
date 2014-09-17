package com.sinosoft.one.scheduler.agent.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinosoft.one.scheduler.agent.exception.AgentException;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bin on 14-5-15.
 * trigger临时文件操作类
 */
public class TriggerFile {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private Charset charset = Charset.forName("UTF-8");// 创建utf-8字符集
    public TriggerFile(){

    }
    /**
     * 读取trigger临时文件创建CronTriggerImpl
     * @return
     */
    public List<CronTriggerImpl> read(){

        List<CronTriggerImpl> cronTriggerList = new ArrayList<CronTriggerImpl>();

        String path = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        File remoteTrigger = new File(path+separator+"trigger"+separator+"remote");
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.endsWith(".trigger")){
                    return  true;
                }
                return false;
            }
        };
        String [] fileList = remoteTrigger.list(filenameFilter);
        for(String fileName:fileList){
            try {
                String str="";
                RandomAccessFile randomAccessFile = new RandomAccessFile(remoteTrigger.getAbsolutePath()+separator+fileName,"rw");
                FileChannel fileChannel = randomAccessFile.getChannel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                int bytesRead = fileChannel.read(byteBuffer);
                while(bytesRead!=-1){
                    byteBuffer.flip();
                    str = String.valueOf(charset.decode(byteBuffer));
                    byteBuffer.clear();
                    bytesRead = fileChannel.read(byteBuffer);
                }
                JSONObject jsonObject = JSON.parseObject(str);
                CronTriggerImpl cronTrigger = new CronTriggerImpl();
                cronTrigger.setCronExpression(jsonObject.getString("cronExpression"));
                cronTrigger.setName(jsonObject.getString("name"));
                cronTrigger.setJobName(jsonObject.getString("jobName"));
                cronTriggerList.add(cronTrigger);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        return cronTriggerList;
    }

    /**
     * 删除trigger临时文件
     */
    public void delete(){

        String path = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        File trigger = new File(path+separator+"trigger"+separator+"local");
        if(trigger.exists()){
            String [] fileList = trigger.list();
            //目录存在时才删除里面的文件
            File temp = null;
            for (int i = 0; i < fileList.length; i++) {
                if (trigger.getAbsolutePath().endsWith(separator)) {
                    temp = new File(trigger+fileList[i]);
                } else {
                    temp = new File(trigger + separator + fileList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }else{
                    AgentException agentException = new AgentException("删除本地trigger临时文件失败!");
                    log.error(agentException.getMessage());
                    throw  agentException;
                }
            }
        }

    }

    public void delete(String triggerName){

        String path = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        File trigger = new File(path+separator+"trigger"+separator+"remote"+separator+triggerName+".trigger");
        trigger.delete();
    }
    /**
     * 根据传入的cronTrigger创建trigger临时文件
     * @param cronTrigger
     */
    public void write(CronTriggerImpl cronTrigger){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobName",cronTrigger.getJobName());
        jsonObject.put("jobGroup",cronTrigger.getJobGroup());
        jsonObject.put("name",cronTrigger.getName());
        jsonObject.put("cronExpression",cronTrigger.getCronExpression());
        String data = jsonObject.toJSONString();
        String separator = System.getProperty("file.separator");
        File trigger = null;
        String path = System.getProperty("user.home");
        //这里区分本地还是远程,本地创建/trigger/local目录,远程创建/trigger/remote目录
        if(AgentConstant.SERVER_IP!=null && AgentConstant.SERVER_PORT!=null){
            trigger = new File(path+separator+"trigger"+separator+"remote");
            trigger.mkdirs();
        }else{
            trigger = new File(path+separator+"trigger"+separator+"local");
            trigger.mkdirs();
        }

        String dir = trigger.getAbsolutePath();
        File triggerFile = new File(dir+separator+cronTrigger.getName()+".trigger");
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(triggerFile,"rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.clear();
            byteBuffer.put(data.getBytes());
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()) {
                try {
                    fileChannel.write(byteBuffer);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    AgentException agentException = new AgentException("创建trigger临时文件失败");
                    throw agentException;
                }
            }
            try {
                fileChannel.force(true);
                fileChannel.close();
            } catch (IOException e) {
                log.error(e.getMessage());
                AgentException agentException = new AgentException("创建trigger临时文件失败");
                throw agentException;
            }finally {
                if(fileChannel!=null){
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.error("临时文件未创建成功"+e.getMessage());
            AgentException agentException = new AgentException("创建trigger临时文件失败");
            throw agentException;
        }
    }

}
