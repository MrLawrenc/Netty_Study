package com.gtdq.netty.client_server_client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:13
 * @description : TODO
 */
@ChannelHandler.Sharable
public class ServerHandler2 extends SimpleChannelInboundHandler<String> {


    //存放所有的客户端连接
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


    //有客户端连接上就会触发的方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("有客户端连接上服务端2啦。。。。。。。。。");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //连接上的客户端存放到ChannelGroup中
        clients.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("服务端2收到消息 ：" + s);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常服务端：" + cause.getMessage());
    }
}