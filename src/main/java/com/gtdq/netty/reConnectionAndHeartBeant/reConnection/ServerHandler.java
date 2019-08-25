package com.gtdq.netty.reConnectionAndHeartBeant.reConnection;

import com.gtdq.netty.util.ExceptionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:44
 * @description : TODO
 */
public class ServerHandler extends SimpleChannelInboundHandler {
    private int lossConnectCount = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        String a=msg.toString();
        if (a.equals("lmy")) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(Thread.currentThread().getName() + " ===> 网二" + a);
        } else {
            TimeUnit.SECONDS.sleep(2);
            System.out.println(Thread.currentThread().getName() + "||||||  张三" + a);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("server exception......" + ExceptionUtil.getExceptionInfo(cause));
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端已断开连接");
        ctx.close();
    }
}