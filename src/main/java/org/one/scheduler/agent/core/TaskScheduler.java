package org.one.scheduler.agent.core;


import com.alibaba.fastjson.JSONObject;
import org.one.scheduler.agent.dto.App;
import org.one.scheduler.agent.exception.AgentException;
import org.one.scheduler.agent.thread.Consumer;
import org.one.scheduler.agent.thread.HeartBeat;
import org.one.scheduler.agent.thread.SendLogCacheTask;
import org.one.scheduler.agent.utils.Connector;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.PropertiesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by bin on 14-5-15.
 * 调度器,用来初始化配置文件,创建任务,计划并调度执行
 */
public class TaskScheduler{

    public static final String PROPERTIES_FILE = "quartz.properties";
    private AgentException initException = null;
    private PropertiesParser cfg;
    private Scheduler scheduler;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String encoding;
    private AgentJobFactory agentJobFactory;

    public PropertiesParser getCfg() {
        return cfg;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public TaskScheduler() {
        this.encoding = System.getProperty("file.encoding");
    }

    /**
     * 初始化Properties文件,给常量赋值

     * @param properties
     */
    public void initProperties(Properties properties){
        BufferedReader bufferedReader = null;
        InputStream is = null;
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);
        try {
            if(is != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(is));
            } else {
                bufferedReader = new BufferedReader(new FileReader(PROPERTIES_FILE));
            }
            properties.load(bufferedReader);
        } catch (IOException ioe) {
            initException = new AgentException("Properties file: '"
                    + PROPERTIES_FILE + "' could not be read.", ioe);
            logger.error(initException.getMessage());
            throw initException;
        }
        finally {
            if(is != null)
                try { is.close(); } catch(IOException ignore) {}
            if(bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                }
            }
        }
        if (cfg != null) {
            return;
        }
        if (initException != null) {

            throw initException;
        }
        this.cfg = new PropertiesParser(properties);
        //给常量赋值
        AgentConstant.APP_NAME = cfg.getStringProperty("app.name",null);
        AgentConstant.SERVER_IP = cfg.getStringProperty("server.ip",null);
        AgentConstant.SERVER_PORT = cfg.getStringProperty("server.port",null);

        if(AgentConstant.APP_NAME == null){
            initException = new AgentException("app.name不能为空");
            logger.error(initException.getMessage());
            throw initException;
        }

        //配置server信息,远程
        if(AgentConstant.SERVER_IP != null && AgentConstant.SERVER_PORT != null){
            //校验格式
            if(!AgentConstant.SERVER_IP.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}")){
                initException = new AgentException("server.ip格式不正确");
                logger.error(initException.getMessage());
                throw initException;
            }else if(!AgentConstant.SERVER_PORT.matches("\\d{4}")){
                initException = new AgentException("server.port格式不正确");
                throw initException;
            }
            String appIp = cfg.getStringProperty("app.ip",null);
            AgentConstant.APP_IP = appIp;
            String appPort = cfg.getStringProperty("app.port",null);
            AgentConstant.APP_PORT = appPort;
            if((appIp == null&&appIp.matches("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}"))||(appPort == null&&appPort.matches("\\d{4}"))){
                initException = new AgentException("app信息配置不完整,请检查配置文件");
                logger.error(initException.getMessage());
                throw initException;
            }else{
                App app = new App();
                app.setIp(appIp);
                app.setName(AgentConstant.APP_NAME);
                app.setPort(appPort);
                String result = Connector.sendApp(app);
                //1.存储是否可连接至server 2.是否给matchKey赋值
                if(result != null){
                    AgentConstant.CONNECT = true;
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String statusCode = jsonObject.getString("statusCode");
                    String errorMsg = jsonObject.getString("errorMsg");
                    if(!statusCode.equals("200")){
                        initException = new AgentException(errorMsg);
                        logger.error(initException.getMessage());
                        throw initException;
                    }
                }
            }
        }
    }


    /**
     * 创建job
     * @return
     */
    public List<JobDetail> createJobDetails(){
        String[] jobName = cfg.getStringArrayProperty("jobName",null);
        if(jobName==null){
            initException  = new AgentException("jobName不能为空");
            logger.error(initException.getMessage());
            throw initException;
        }
        agentJobFactory = new AgentJobFactory();
        Map<String,Business> businessMap = agentJobFactory.getBusinessMap();
        List<JobDetail> jobDetails = new ArrayList<JobDetail>();
        for(int i=0;i<jobName.length;i++){
            String job = jobName[i];
            String jobClass = cfg.getStringProperty(job + ".jobClass", null);
            if(jobClass==null){
                initException = new AgentException(job+"对应的jobClass不能为空");
                logger.error(initException.getMessage());
                throw initException;
            }
            try {
                Business business = (Business)Class.forName(jobClass).newInstance();
                businessMap.put(job,business);
                JobDetail jobDetail = JobBuilder.newJob(Task.class).withIdentity(job).storeDurably(true).build();
                jobDetails.add(jobDetail);
            } catch (ClassNotFoundException e) {
                initException = new AgentException("jobClass配置不正确");
                logger.error(initException.getMessage());
                throw initException;
            } catch (InstantiationException e) {
                initException = new AgentException("jobClass创建失败");
                logger.error(initException.getMessage());
                throw initException;
            } catch (IllegalAccessException e) {
                initException = new AgentException("jobClass创建失败");
                logger.error(initException.getMessage());
                throw initException;
            }
        }
        return jobDetails;
    }

    /**
     * 构建agent
     */
    public void start(){

        Properties properties = new Properties();
        initProperties(properties);
        StdSchedulerFactory factory = null;
        try {
            factory = new StdSchedulerFactory(PROPERTIES_FILE);
            scheduler = factory.getScheduler();
        } catch (SchedulerException e) {
            logger.error("创建scheduler失败" + e.getMessage());
            initException = new AgentException("创建scheduler失败"+e.getMessage());
            throw initException;
        }
        List<JobDetail> jobDetails = createJobDetails();
        for(JobDetail jobDetail:jobDetails){
            try {
                scheduler.addJob(jobDetail,false);
            } catch (SchedulerException e) {
                logger.error("创建job失败" + e.getMessage());
                initException = new AgentException("创建job失败"+e.getMessage());
                throw initException;
            }
        }
        //创建trigger
        AbstractTrigger abstractTrigger = AbstractTrigger.build();
        abstractTrigger.trigger(cfg,scheduler);

        try {
            scheduler.setJobFactory(agentJobFactory);
            scheduler.start();
        } catch (SchedulerException e) {
            initException = new AgentException("agent启动失败"+e.getMessage());
            throw initException;
        }
        try {
            scheduler.getListenerManager().addJobListener(new AgentJobListener());
            scheduler.getListenerManager().addTriggerListener(new AgentTriggerListener());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
        scheduledExecutorService.scheduleAtFixedRate(new Consumer(),0,10, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(new SendLogCacheTask(),60,60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new HeartBeat(),0,30, TimeUnit.SECONDS);
    }

}
