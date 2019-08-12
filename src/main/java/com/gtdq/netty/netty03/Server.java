package com.gtdq.netty.netty03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:12
 * @description : TODO
 */
public final class Server {

    private static final String ip = "127.0.0.1";
    private static final int port = 9527;


    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ServerInitializer());


            //需要三次握手，所以同步等待客户端连接
            ChannelFuture future = b.bind(ip, port).sync();
            //监听服务端关闭，并阻塞
            future.channel().closeFuture().sync();
            System.out.println("server start");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭两个EventLoopGroup对象
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}