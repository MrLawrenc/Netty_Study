package com.gtdq.netty.reConnectionAndHeartBeant.reConnection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:43
 * @description : TODO
 */
@Getter
public class NettyServer {
    public static void main(String[] args) throws Exception {
        NettyServer nettyServer = new NettyServer(9527);


        //测试关闭服务器功能
        TimeUnit.SECONDS.sleep(3);
        System.out.println("即将关闭server............");
        nettyServer.closeServer();
    }

    private ChannelFuture future;

    public NettyServer(int port) {
        init(port);
    }

    protected void init(int port) {
        ServerBootstrap b = new ServerBootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(5);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(5);
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                /*
                 *     BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，
                 *     用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
                 * */
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new ServerHandler());
                    }
                });

        try {
            future = b.bind(port).sync();
            //不阻塞 调用future.channel的close即关闭
            future.channel().closeFuture().addListener(future -> {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("-----------------");
        }
//        future.channel().closeFuture().sync();阻塞
    }


    /**
     * @author : LiuMing
     * @date : 2019/8/24 17:05
     * @description :   关闭通道，触发关闭服务器的监听
     */
    public void closeServer() {
        future.channel().close();
    }
}