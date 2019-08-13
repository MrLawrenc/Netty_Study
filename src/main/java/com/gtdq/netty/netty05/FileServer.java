package com.gtdq.netty.netty05;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * @author : LiuMingyao
 * @date : 2019/8/12 11:31
 * @description : 服务器给客户端发送文件，采用telnet连接服务器，连接上之后输入文件路径会返回文件信息
 * <p>如输入:  E:\MyProject\study_netty\src\main\java\com\gtdq\netty\netty05\a.txt  控制台会输出文件内容</p>
 */

public class FileServer
{
    public void run (int port)throws Exception{
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try
        {
            ServerBootstrap b=new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>()
                    {

                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception
                        {
                            ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8),
                                    //按照回车换行符对数据包进行解码
                                    new LineBasedFrameDecoder(1024),
                                    new StringDecoder(CharsetUtil.UTF_8),
                                    new com.gtdq.netty.netty.netty05.FileServerHandler());
                        }
                    });
            ChannelFuture f=b.bind(port).sync();
            System.out.println("Start netty06_file server at port : "+port);
            f.channel().closeFuture().sync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args)throws Exception
    {
        int port =8888;
        try
        {
            if (args!=null&&args.length>0)
            {
                port=Integer.valueOf(args[0]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        new FileServer().run(port);
    }
}