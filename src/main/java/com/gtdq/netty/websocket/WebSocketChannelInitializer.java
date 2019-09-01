package com.gtdq.netty.websocket;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author : LiuMingyao
 * @date : 2019/8/9 14:06
 * @description : 初始化通道，可以在这儿加载自定义的业务处理handler
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //添加一个http编解码器
        pipeline.addLast(new HttpServerCodec());
        //添加一个用于支持大数据流的
        pipeline.addLast(new ChunkedWriteHandler());
        //添加一个聚合器，将HttpMessage聚合为FullHttpResquest/response,xiao xi taichang hui bei fengduan fa song zhihou juhe
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));
        //指定访问路由.ru : ws://localhost:9527/ws
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //=================上面是netty和http结合需要的管道处理器=================

        //添加自定义的handler
        pipeline.addLast(new ChatHandler());
    }
}