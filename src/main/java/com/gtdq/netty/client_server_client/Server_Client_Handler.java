package com.gtdq.netty.client_server_client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:13
 * @description : TODO
 */
public class Server_Client_Handler extends ChannelInboundHandlerAdapter {


    //存放所有的客户端连接
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    EventLoop eventLoop;

    //有客户端连接上就会触发的方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有客户端连接上啦。。。。。。。。。");

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //连接上的客户端存放到ChannelGroup中
        clients.add(ctx.channel());

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object s) throws Exception {

        System.out.println("中转站服务端收到消息 ：" + s);

        /**
         * 当有客户端发送消息来得时候，就连接上下个服务端（此时这个服务端就作为客户端），当然在channelRead0调用也可以
         * 重点在于client和server使用同一个group，使用ctx.channel().eventLoop()来获取。
         */
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8)).addLast(new StringEncoder(CharsetUtil.UTF_8))
                                .addLast("client2", new ClientHandler());
                    }
                }).group(ctx.channel().eventLoop());
        ChannelFuture connect = bootstrap.connect("127.0.0.1", 6667);
        connect.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.out.println(connect.channel());
                connect.channel().writeAndFlush(s);
                connect.addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("异常服务端1：");
    }
}