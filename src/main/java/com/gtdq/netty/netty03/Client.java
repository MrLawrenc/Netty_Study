package com.gtdq.netty.netty03;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author : LiuMingyao
 * @date : 2019/8/5 22:26
 * @description : TODO
 */
public class Client {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientInitializer());

            ChannelFuture connect = bootstrap.connect("127.0.0.1", 9527).sync();

            ChannelFuture lastWriteFuture = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Channel ch = connect.channel();
           for (;;){
                String line = reader.readLine();
                if (line == null) break;

                lastWriteFuture=ch.writeAndFlush(line+"/r/n");

                if ("bye".equals(line.toLowerCase())){
                    ch.closeFuture().sync();
                    break;
                }

                if (lastWriteFuture!=null) lastWriteFuture.sync();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}