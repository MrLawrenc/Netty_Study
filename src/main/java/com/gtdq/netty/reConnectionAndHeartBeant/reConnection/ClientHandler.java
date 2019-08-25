package com.gtdq.netty.reConnectionAndHeartBeant.reConnection;

import com.gtdq.netty.util.ExceptionUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:57
 * @description : TODO
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    private final NettyClient client;
    private int count;

    public ClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        count = 0;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //断线重连
       client.doReconnection();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("client异常。。。。。" + ExceptionUtil.getExceptionInfo(cause));
        ctx.close();
    }

}