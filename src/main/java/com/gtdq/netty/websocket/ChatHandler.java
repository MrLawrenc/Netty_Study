package com.gtdq.netty.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : LiuMingyao
 * @date : 2019/8/9 14:31
 * @description : TODO
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    //存放所有连接上的客户端
    private  static final ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    static Map<String, Channel> map = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    //当服务端接收到了数据会自动调用
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

        //获取客户端发来的消息
        String text = textWebSocketFrame.text();  //格式："2"+msg+":xm"  第一个代表消息类型（1是建立连接 2是发送消息） 第二个是具体的消息内容 第三个是用户名
        String type = text.split(":")[0];
        String name = text.split(":")[2];
        String msg = text.split(":")[1];
        logger.info("该条客户端消息是:{}", type.equals("1")?"建立连接":"聊天");
        logger.info("{}客户端发来消息:{}", name, msg.toString());

        if (type.equals("1")){
            //将用户和channel关联绑定
            map.put(name,channelHandlerContext.channel());
        }else {
            String receiceClient="xm".equals(name)?"xh":"xm";
             map.get(receiceClient).writeAndFlush(new TextWebSocketFrame(name+"说:"+msg));
        }
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }
}