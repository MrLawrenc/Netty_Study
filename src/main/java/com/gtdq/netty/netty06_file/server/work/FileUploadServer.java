package com.gtdq.netty.netty06_file.server.work;

import com.gtdq.netty.netty06_file.server.handler.FileUploadServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:21
 * @description :   TODO
 */
public class FileUploadServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadServer.class);

    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            /**
             * BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
             * 如果未设置或所设置的值小于1，Java将使用默认值50
             */
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    LOGGER.info("有客户端连接上来:" + ch.localAddress().toString());
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
                    ch.pipeline().addLast(new FileUploadServerHandler());
                }
            });
            ChannelFuture f = b.bind(port).sync();
            f.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                }
            });
            LOGGER.info("netty06_file server 等待连接：");
            f.channel().closeFuture().sync();
            LOGGER.info("netty06_file end");
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
