package com.sinosoft.one.scheduler.agent.thread;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by bin on 14-6-4.
 */
public class HeartBeatTest {

    public static void main(String[] args){
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("HeartBeat");
            }
        },0,60, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("SendLog");
            }
        },10,60, TimeUnit.SECONDS);
    }
}
