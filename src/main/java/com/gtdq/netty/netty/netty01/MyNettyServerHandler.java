package com.gtdq.netty.netty.netty01;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:13
 * @description : TODO
 */
public class MyNettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接上客户端");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client  "+ctx.channel().remoteAddress()+"  connected");
        ctx.channel().writeAndFlush("server get  msg"+msg);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常服务端："+cause.getMessage());
    }
}