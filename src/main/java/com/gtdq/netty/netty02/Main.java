package com.gtdq.netty.netty02;

/**
 * @author : LiuMingyao
 * @date : 2019/8/9 0:13
 * @description : TODO
 */
public class Main {
    public static void main(String[] args) {
        new Thread(new NettyClient(), ">>>> this thread main").start();
    }
}