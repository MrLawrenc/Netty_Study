package com.gtdq.netty.netty02;

import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Date;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:13
 * @description : TODO
 */
@ChannelHandler.Sharable
public class MyNettyServerHandler extends SimpleChannelInboundHandler<String> {


    //存放所有的客户端连接
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);


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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        System.out.println("服务端收到消息 ：" + s);


        //当有新的客户端连上时也会给其他还连接着的客户端发消息
        for (Channel client : clients) {
            ChannelFuture future = client.writeAndFlush(new Date() + ":服务器给"+client.remoteAddress()+"发消息" + s);

        }
//        clients.forEach(connection->{
//            connection.writeAndFlush(new Date()+":"+s);
//            System.out.println("添加一个:"+channelHandlerContext.channel().remoteAddress());
//        });


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常服务端：" + cause.getMessage());
    }
}