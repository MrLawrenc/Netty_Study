package com.gtdq.netty.netty06_file.client.work;


import com.gtdq.netty.netty06_file.client.handler.FileUploadClientHandler;
import com.gtdq.netty.netty06_file.model.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */

public class FileUploadClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadClient.class);

    public void connect(int port, String host,
                        final FileUploadFile fileUploadFile) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new ObjectEncoder());

                            //ObjectDecoder第一个参数是:对象序列化后的最大字节数组长度设置，可以设为1024*1024，即为防止异常码流和解码错位导致的内存溢出，将对象序列化后的最大字节数组长度设为1M
                            //创建线程安全的WeakReferenceMap对类加载器进行缓存,后面可以跟this.getClass().getClassLoader，也可以直接设为null
                            pipeline.addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));

                            pipeline.addLast(new FileUploadClientHandler(fileUploadFile));
                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
            LOGGER.info("Client connect()结束");
        } finally {
            group.shutdownGracefully();
        }
    }

}
