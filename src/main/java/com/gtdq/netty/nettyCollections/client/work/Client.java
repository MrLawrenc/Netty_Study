package com.gtdq.netty.nettyCollections.client.work;

import com.gtdq.netty.nettyCollections.client.handler.ClientHandler;
import com.gtdq.netty.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */

public class Client {
    private final static Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static Client client;
    private ChannelFuture future;

    private static Bootstrap bootstrap = new Bootstrap();
    private static NioEventLoopGroup group = new NioEventLoopGroup();

    private String ip;
    private int port;
    @Getter
    private boolean needConnection = false;
    //断线重连次数
    private int reConnectionCount;
    private boolean flag = true;


    public void setNeedConnection(boolean needConnection) {
        this.needConnection = needConnection;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:23
     * @description :   获取单例客户端对象
     */
    public static Client getInstance(String ip, int port) {
        if (null == client) {
            synchronized (Client.class) {
                if (null == client) {
                    client = new Client(ip, port);
                }
            }
        }
        return client;
    }

    protected Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/24 16:20
     * @description :   初始化client对象(bootstrap的初始化)
     */
    public Client init() {
        Client client = this;
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ObjectEncoder());

                        //ObjectDecoder第一个参数是:对象序列化后的最大字节数组长度设置，可以设为1024*1024，即为防止异常码流和解码错位导致的内存溢出，将对象序列化后的最大字节数组长度设为1M
                        //创建线程安全的WeakReferenceMap对类加载器进行缓存,后面可以跟this.getClass().getClassLoader，也可以直接设为null
                        pipeline.addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));
                        pipeline.addLast(new ClientHandler(client));
                    }
                });
        return this;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:18
     * @description :   获取一个连接(该连接是当前客户端对象持有的)
     */
    public ChannelFuture getSelfChannelFuture() throws InterruptedException {
        return null == future || !future.channel().isActive() ? this.future = getNewChannelFuture() : this.future;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/24 16:41
     * @description :   获取一个新的ChannelFuture
     */
    public ChannelFuture getNewChannelFuture() throws InterruptedException {
        return getChannelFutureList(this.ip, this.port, 1).get(0);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:14
     * @description :   获取多连接集合
     * <p>客户端需要多个连接的时候使用</p>
     */
    public List<ChannelFuture> getChannelFutureList(String ip, int port, int connectionNum) throws InterruptedException {
        List<ChannelFuture> channelFutures = new ArrayList<>(connectionNum);
        for (int i = 0; i < connectionNum; i++) {
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            channelFutures.add(future);
        }
        return channelFutures;
    }

    public void closeClient() {
        if (future == null) return;
        LogUtil.infoLog("客户端主动关闭channel");
        future.channel().close();
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:12
     * @description :   关闭线程池
     */
    public void destory() {
        group.shutdownGracefully();
    }


    /**
     * @author : LiuMing
     * @date : 2019/8/25 17:36
     * @description :   使用定时器来不断重连服务器
     * <code>
     */
    public void doReconnection() {

        Client client = this;
        if (reConnectionCount < 4) {
            reConnectionCount++;
            int timeout = 2000 * reConnectionCount;
            Timer timer = new Timer();
            LOGGER.info("第{}次重连将在{}ms之后进行!", reConnectionCount, timeout);
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    ChannelFuture future = bootstrap.connect(ip, port);
                    future.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture f) throws Exception {
                            //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                            if (f.isSuccess()) {
                                LOGGER.info("第{}次重连成功.............", reConnectionCount);
                                reConnectionCount = 0;
                            } else {
                                LOGGER.info("第{}次重连失败.............", reConnectionCount);
                                doReconnection();
                            }
                        }
                    });
                }
            }, timeout);
        } else {
            LOGGER.info("重连次数过多，不进行重连，即将推出客户端............");
            flag = false;
            System.exit(0);
        }

    }


}
