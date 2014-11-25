package org.one.scheduler.agent.utils;

import org.one.scheduler.agent.core.AgentConstant;
import org.one.scheduler.agent.dto.App;
import org.one.scheduler.agent.dto.Log;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bin on 14-5-15.
 * 用来和server进行通信
 */
public class Connector{

    private static HttpClient httpClient;
    private final static Logger logger = LoggerFactory.getLogger(Connector.class);
    private static HttpHost host;
    public static HttpClient getHttpClient() {
       host = new HttpHost("172.16.251.58",3128);
        // 设置组件参数, HTTP协议的版本,1.1/1.0/0.9
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1");
        HttpProtocolParams.setUseExpectContinue(params, true);

        //设置连接超时时间
        int REQUEST_TIMEOUT = 10*1000;  //设置请求超时10秒钟
        int SO_TIMEOUT = 10*1000;       //设置等待数据超时时间10秒钟
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, REQUEST_TIMEOUT);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);

        //设置访问协议
        SchemeRegistry schreg = new SchemeRegistry();
        schreg.register(new Scheme("http",80, PlainSocketFactory.getSocketFactory()));
        schreg.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        //多连接的线程安全的管理器
        PoolingClientConnectionManager pccm = new PoolingClientConnectionManager(schreg);
        pccm.setDefaultMaxPerRoute(20); //每个主机的最大并行链接数
        pccm.setMaxTotal(100);          //客户端总并行链接最大数

        DefaultHttpClient httpClient = new DefaultHttpClient(pccm, params);
        return httpClient;
    }



    /**
     * 发送应用信息
     * @param app
     * @return
     */
    public static String sendApp(App app){

        String result = null;
        String url = "http://"+ AgentConstant.SERVER_IP+":"+AgentConstant.SERVER_PORT+"/business_monitor/app/save";
        HttpPost httpPost = new HttpPost(url);
        httpClient = getHttpClient();
        //设置报文头
        //"content-type", "application/x-www-form-urlencoded"
        //"Content-Type", "text/xml;charset=UTF-8"
        httpPost.setHeader("content-type", "application/x-www-form-urlencoded");
        //httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,host);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ip", app.getIp()));
        params.add(new BasicNameValuePair("name", app.getName()));
        params.add(new BasicNameValuePair("port", app.getPort()));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            logger.error("转换发送信息失败"+e.getMessage());
        }

        // 发送请求
        HttpResponse httpResponse = null;
        InputStream in = null;
        try {
            httpResponse = httpClient.execute(httpPost);
        } catch (IOException e) {
            logger.error("发送app信息失败" + e.getMessage());
        }
        if(httpResponse != null){
            HttpEntity entity = httpResponse.getEntity();
            try {
                result = EntityUtils.toString(entity);
                in = entity.getContent();
            } catch (IOException e) {
                logger.error("转换返回信息失败" + e.getMessage());
            }finally {
                if(in!=null){
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return result;
    }

    /**
     * 发送任务执行开始信息
     * @param log
     * @return
     */
    public static String sendStartLog(Log log){
        String result = null;
        if(AgentConstant.SERVER_IP!=null&&AgentConstant.SERVER_PORT!=null){
            String url = "http://"+AgentConstant.SERVER_IP+":"+AgentConstant.SERVER_PORT+"/business_monitor/trigger/log/startRecord";
            HttpPost httpPost = new HttpPost(url);
            httpClient = getHttpClient();
            //设置请求超时
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("appName", log.getAppName()));
            params.add(new BasicNameValuePair("jobName", log.getJobName()));
            params.add(new BasicNameValuePair("triggerName",log.getTriggerName()));
            params.add(new BasicNameValuePair("beginTime", log.getStartTime()));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("转换发送信息失败"+e.getMessage());
            }
            //设置报文头
            httpPost.setHeader("content-type", "application/x-www-form-urlencoded");
            // 发送请求
            HttpResponse httpResponse = null;
            InputStream is = null;
            try {
                httpResponse = httpClient.execute(httpPost);
            } catch (IOException e) {
                logger.error("发送日志开始信息失败" + e.getMessage());
            }
            if(httpResponse != null){
                HttpEntity entity = httpResponse.getEntity();
                try {
                    result = EntityUtils.toString(entity);
                    is = entity.getContent();
                } catch (IOException e) {
                    logger.error("转换返回信息失败" + e.getMessage());
                }finally {
                    if(is!=null){
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * 发送任务结束信息
     * @param log
     * @return
     */
    public static String sendEndLog(Log log){

        String result = null;
        if(AgentConstant.SERVER_IP!=null&&AgentConstant.SERVER_PORT!=null){
            String url = "http://"+AgentConstant.SERVER_IP+":"+AgentConstant.SERVER_PORT+"/business_monitor/trigger/log/endRecord";
            HttpPost httpPost = new HttpPost(url);
            httpClient = getHttpClient();
            //设置请求超时
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", log.getId()));
            params.add(new BasicNameValuePair("appName", log.getAppName()));
            params.add(new BasicNameValuePair("jobName", log.getJobName()));
            params.add(new BasicNameValuePair("triggerName",log.getTriggerName()));
            params.add(new BasicNameValuePair("beginTime", log.getStartTime()));
            params.add(new BasicNameValuePair("endTime", log.getEndTime()));
            params.add(new BasicNameValuePair("exceptionMsg", log.getExceptionMsg()));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("转换发送信息失败"+e.getMessage());
            }
            //设置报文头
            httpPost.setHeader("content-type", "application/x-www-form-urlencoded");
            // 发送请求
            HttpResponse httpResponse = null;
            InputStream is = null;
            try {
                httpResponse = httpClient.execute(httpPost);
            } catch (IOException e) {
                logger.error("发送日志结束信息失败" + e.getMessage());
            }
            if(httpResponse != null){
                HttpEntity entity = httpResponse.getEntity();
                try {
                    result = EntityUtils.toString(entity);
                    is = entity.getContent();
                } catch (IOException e) {
                    logger.error("转换返回信息失败" + e.getMessage());
                }finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return result;
    }

    /**
     * 发送日志缓存文件
     * @return
     */
    public static String sendLog(String fileName){
        File file = new File(fileName);
        String result = null;
        if(AgentConstant.SERVER_IP!=null&&AgentConstant.SERVER_PORT!=null){
            String url = "http://"+AgentConstant.SERVER_IP+":"+AgentConstant.SERVER_PORT+"/business_monitor/trigger/log/saveRecord";
            HttpPost httpPost = new HttpPost(url);
            httpClient = getHttpClient();
            //设置请求超时
            FileBody fileBody = new FileBody(file);
            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("logFile", fileBody);
            httpPost.setEntity(reqEntity);
            HttpResponse httpResponse = null;
            InputStream is = null;
            try {
                httpResponse = httpClient.execute(httpPost);
            } catch (IOException e) {
                logger.error("发送日志缓存信息失败" + e.getMessage());
            }
            if(httpResponse != null){
                HttpEntity entity = httpResponse.getEntity();
                try {
                    result = EntityUtils.toString(entity);
                    is = entity.getContent();
                } catch (IOException e) {
                    logger.error("转换返回信息失败" + e.getMessage());
                }finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return result;
    }

    /**
     * 发送心跳信息
     * @return
     */
    public static void HeartBeat(String appName){

        if(AgentConstant.SERVER_IP!=null&&AgentConstant.SERVER_PORT!=null){
            String url = "http://"+AgentConstant.SERVER_IP+":"+AgentConstant.SERVER_PORT+"/business_monitor/app/heartBeat";
            HttpPost httpPost = new HttpPost(url);
            httpClient = getHttpClient();
            //设置请求超时
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("appName", appName));
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error("转换发送信息失败"+e.getMessage());
            }
            //设置报文头
            httpPost.setHeader("content-type", "application/x-www-form-urlencoded");
            // 发送请求
            HttpResponse httpResponse = null;
            InputStream is = null;
            try {
                httpResponse = httpClient.execute(httpPost);
                is = httpResponse.getEntity().getContent();
            } catch (IOException e) {
                logger.error("发送app心跳失败" + e.getMessage());
            }finally {
                if(is!=null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
