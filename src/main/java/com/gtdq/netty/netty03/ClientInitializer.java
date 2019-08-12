package com.gtdq.netty.netty03;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author : LiuMingyao
 * @date : 2019/8/6 22:39
 * @description : TODO
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private static final StringDecoder STRING_DECODER=new StringDecoder();
    private static final StringEncoder STRING_ENCODER=new StringEncoder();
    private static final ClientHandler CLIENT_HANDLER=new ClientHandler();

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();
        //添加帧限定符来防止粘包
        pipeline.addLast(new DelimiterBasedFrameDecoder(9527, Delimiters.lineDelimiter()));
        //解码
        pipeline.addLast(STRING_ENCODER).addLast(STRING_DECODER);
        //业务逻辑处理类
        pipeline.addLast(CLIENT_HANDLER);
    }

}