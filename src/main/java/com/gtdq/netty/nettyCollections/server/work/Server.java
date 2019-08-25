package com.gtdq.netty.nettyCollections.server.work;

import com.gtdq.netty.nettyCollections.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:21
 * @description :   TODO
 */
public class Server {
    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private ChannelFuture future;
    //收到的文件存储路径
    private String filePath;
    private int port;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private final static ServerBootstrap b = new ServerBootstrap();


    public Server() {
        this("E:/temp");
    }

    public Server(String filePath) {
        this.filePath = filePath;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
    }

    public Server(int bossGroupNum, int workGroupNum) {
        this("E:/temp", bossGroupNum, workGroupNum);
    }

    public Server(String filePath, int bossGroupNum, int workGroupNum) {
        this.filePath = filePath;
        bossGroup = new NioEventLoopGroup(bossGroupNum);
        workerGroup = new NioEventLoopGroup(workGroupNum);
    }

    public Server init(int port) throws Exception {
        Server server = this;
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                /*
                 *     BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，
                 *     用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
                 * */
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ObjectEncoder());
                        pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
                        pipeline.addLast(new ServerHandler(server.filePath));
                    }
                });
        future = b.bind(port).sync();
        //监听future的lose
        future.channel().closeFuture().addListener(future -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        });
        return this;
    }

    public void closeServer() {
        this.future.channel().close();
    }
}
