package com.gtdq.netty.nettyCollections.client.work;

import com.gtdq.netty.nettyCollections.client.handler.ClientHandler;
import com.gtdq.netty.util.ExceptionUtil;
import com.gtdq.netty.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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
    private KafkaTemplate<String, String> kafkaTemplate;


    public void setNeedConnection(boolean needConnection, KafkaTemplate<String, String> kafkaTemplate) {
        this.needConnection = needConnection;
        this.kafkaTemplate = kafkaTemplate;
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
                        pipeline.addLast("myHandler", new ClientHandler(client));
                    }
                });
        return this;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:18
     * @description :   获取一个连接(该连接是当前客户端对象持有的)
     */
    public ChannelFuture getSelfChannelFuture() {
        return null == future || !future.channel().isActive() ? this.future = getNewChannelFuture() : this.future;
    }

    public ChannelHandlerContext test() {
        ClientHandler myHandler = (ClientHandler) client.getSelfChannelFuture().channel().pipeline().get("myHandler");//得到自己的handler
        /**
         * 获得自己Handler里面的ctx，这个对象也可以write，和channel的write不同的是：
         * ctx的write是从自身这个Handler开始，经过在它之前定义的Handler，依次执行的
         * 而channel的write则是从pipeline的最后一个Handler开始向前依次执行
         * 因此:当不需要经过所有的pipeline链时，使用ctx的Handler可以提高性能
         */
        ChannelHandlerContext ctx = myHandler.getCtx();
        return ctx;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/24 16:41
     * @description :   获取一个新的ChannelFuture
     */
    public ChannelFuture getNewChannelFuture() {
        return getChannelFutureList(this.ip, this.port, 1).get(0);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:14
     * @description :   获取多连接集合
     * <p>客户端需要多个连接的时候使用</p>
     */
    public List<ChannelFuture> getChannelFutureList(String ip, int port, int connectionNum) {
        List<ChannelFuture> channelFutures = new ArrayList<>(connectionNum);
        for (int i = 0; i < connectionNum; i++) {
            ChannelFuture future = null;
            try {
                future = bootstrap.connect(ip, port).sync();
            } catch (InterruptedException e) {
                LogUtil.errorLog("client获取future异常，" + ExceptionUtil.getExceptionInfo(e, true));
            }
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
                                LOGGER.info("第{}次重连成功(激活kafka重新发送失败的数据).............", reConnectionCount);
                                reConnectionCount = 0;
                                ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send("sendFile", "ConnectionAvailable", "true");
                                send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        LogUtil.warnLog("kafka没有收到连接可用的状态消息,不会立即重发之前发送失败的消息.............");
                                    }

                                    @Override
                                    public void onSuccess(SendResult<String, String> stringStringSendResult) {
                                        LogUtil.infoLog("kafka收到连接可用的状态消息，即将重发之前发送失败的消息.............");
                                    }
                                });
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
