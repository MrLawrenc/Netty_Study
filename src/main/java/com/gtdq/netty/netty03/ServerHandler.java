package com.gtdq.netty.netty03;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : LiuMingyao
 * @date : 2019/8/6 22:25
 * @description : 继承ChannelInboundHandlerAdapter也可以，这个类是SimpleChannelInboundHandler的父类
 */
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("welcome to " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg+"======");
        String response = null;
        boolean close = false;
        if (response.isEmpty()) {
            response = "please type something /r/n";
        } else if ("bye".equals(msg)) {
            response = "have a good day/r/n";
            close = true;
        } else {
            response = "did you say '" + msg + ", ?/r/n";
        }

        ChannelFuture f = ctx.write(response);

        if (close) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    //会调用上面的方法，上面的方法来自SimpleChannelInboundHandler的父类ChannelInboundHandlerAdapter
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        channelRead(channelHandlerContext, s);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}