package com.gtdq.netty.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author : LiuMingyao
 * @date : 2019/8/14 21:01
 * @description : springboot的定时任务
 */
@Component
public class timer {

    //设置默认值的格式
    @Scheduled(cron = "${mars.startTime:1 19 21 * * ?}")
    public void start() {
        System.out.println("开始定时任务");
    }
}