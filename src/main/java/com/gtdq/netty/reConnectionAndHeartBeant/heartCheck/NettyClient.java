package com.gtdq.netty.reConnectionAndHeartBeant.heartCheck;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:56
 * @description : TODO
 */

public class NettyClient {
    private ChannelFuture future;
    private Bootstrap bootstrap;
    private NioEventLoopGroup group;

    private String ip;
    private int port;


    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    protected void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new HeartBeatClientHandler(bootstrap));
                    }
                });
    }

    public ChannelFuture getChannelFuture() {
        if (null == bootstrap) init();

        if (null == future || !future.channel().isActive()) {
            try {
                System.out.println("连接服务端.......");
                future = bootstrap.connect(ip, port).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //不阻塞代码,关闭客户端的监听
            future.channel().closeFuture().addListener(future -> group.shutdownGracefully());
        }
        return future;
    }

    public void close() throws Exception {
        if (null == future) {
            return;
        }
        future.channel().close();
    }

    public void destory() {
        if (null == group) {
            return;
        }
        group.shutdownGracefully();
    }
}