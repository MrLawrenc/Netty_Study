package com.gtdq.netty.reConnectionAndHeartBeant.heartCheck;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:57
 * @description : TODO
 */
public class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {
    private Bootstrap bootstrap;

    public HeartBeatClientHandler(Bootstrap bootstrap){
        this.bootstrap=bootstrap;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }



    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("client断开。。。");
        ctx.close();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("客户端循环心跳监测发送: " + new Date());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush("biubiu");
                TimeUnit.SECONDS.sleep(11);
                ctx.writeAndFlush("我应该被服务端kill了。。。。。。");
            }
        }
    }
}