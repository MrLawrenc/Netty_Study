package com.gtdq.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author : LiuMingyao
 * @date : 2019/8/9 14:06
 * @description : TODO
 */
public class WebSocketNettyServer {

    public static void main(String[] args) {

        //主线程池:负责接收连接
        NioEventLoopGroup mainGroup = new NioEventLoopGroup();
        //从线程池:主要负责io读写
        NioEventLoopGroup subGroup = new NioEventLoopGroup();
        try {

            //netty服务器启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(mainGroup, subGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketChannelInitializer());


            ChannelFuture future = bootstrap.bind(9527).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭线程池
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }

    }
}