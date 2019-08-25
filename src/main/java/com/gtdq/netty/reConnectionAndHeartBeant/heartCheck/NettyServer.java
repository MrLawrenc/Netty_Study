package com.gtdq.netty.reConnectionAndHeartBeant.heartCheck;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:43
 * @description : TODO
 */
@Getter
public class NettyServer {
    private ChannelFuture future;

    public NettyServer(int port) {
        init(port);
    }

    protected void init(int port) {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(16);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new HeartBeatServerHandler());
                    }
                });

        try {
            future = b.bind(port).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //不阻塞
        future.channel().closeFuture().addListener(future -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        });
//        future.channel().closeFuture().sync();阻塞
    }


    public void closeServer() {
        future.channel().close();
    }
}