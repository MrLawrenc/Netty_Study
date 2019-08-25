package com.gtdq.netty.reConnectionAndHeartBeant.heartCheck;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 15:05
 * @description : TODO
 */
public class MainHeartBeat {
    public static void main(String[] args) throws Exception {
        NettyServer nettyServer = new NettyServer(9527);
        TimeUnit.SECONDS.sleep(2);

        NettyClient nettyClient = new NettyClient("127.0.0.1", 9527);
        ChannelFuture future = nettyClient.getChannelFuture();

        System.out.println("===需要阻塞主线程===等待netty线程执行");
        TimeUnit.SECONDS.sleep(20);



        if (!future.channel().isOpen()) {
            System.out.println("客户端已断开，即将断开服务端.......");
            nettyServer.closeServer();
        }

    }
}