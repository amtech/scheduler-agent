package org.one.scheduler.agent.dto;

/**
 * Created by bin on 14-5-15.
 * 应用信息
 */
public class App {

    //应用名
    private String name;
    //应用IP
    private String ip;
    //应用端口
    private String port;
    //匹配码
    private String matchKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }
}
