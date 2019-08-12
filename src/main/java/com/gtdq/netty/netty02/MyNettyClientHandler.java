package com.gtdq.netty.netty02;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:34
 * @description : TODO
 */
public class MyNettyClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        Channel channel = channelHandlerContext.channel();
        System.out.println("server ip :"+channel.remoteAddress());
        System.out.println("客户端收到消息："+s);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常服务端："+cause.getMessage());
        ctx.close();
    }
}