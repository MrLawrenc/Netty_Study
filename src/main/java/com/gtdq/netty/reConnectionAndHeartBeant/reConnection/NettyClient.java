package com.gtdq.netty.reConnectionAndHeartBeant.reConnection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/23 14:56
 * @description : TODO
 */

public class NettyClient {
    public static NettyClient client;
    private ChannelFuture future;

    private static Bootstrap bootstrap = new Bootstrap();
    private static NioEventLoopGroup group = new NioEventLoopGroup();

    private String ip;
    private int port;
    //断线重连次数
    private int reConnectionCount;
    private boolean flag = true;

    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:23
     * @description :   获取单例客户端对象
     */
    public static NettyClient getInstance(String ip, int port) {
        if (null == client) {
            synchronized (NettyClient.class) {
                if (null == client) {
                    client = new NettyClient(ip, port);
                }
            }
        }
        return client;
    }

    protected NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/24 16:20
     * @description :   初始化client对象(bootstrap的初始化)
     */
    public NettyClient init() {
        NettyClient client = this;
        bootstrap.group(group).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new StringDecoder());
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
    public ChannelFuture getChannelFuture() throws InterruptedException {
        return null == future || !future.channel().isActive() ? getNewChannelFuture() : this.future;
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


    /**
     * @author : LiuMing
     * @date : 2019/8/23 22:12
     * @description :   关闭线程池
     */
    public void destory() {
        group.shutdownGracefully();
    }


    public void doReconnection() {
        NettyClient client = this;
        if (reConnectionCount < 4) {
            reConnectionCount++;
            int timeout = 2000 * reConnectionCount;
            Timer timer = new Timer();
            System.out.println("第" + reConnectionCount + "次重连将在" + timeout + "ms之后进行!");
            timer.schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    ChannelFuture future = bootstrap.connect(ip, port);
                    future.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture f) throws Exception {
                            //如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
                            if (f.isSuccess()) {
                                System.out.println("重连成功.............");
                                reConnectionCount = 0;
                            } else {
                                System.out.println("重连失败.............");
                                doReconnection();
                            }
                        }
                    });
                }
            }, timeout);
        } else {
            System.out.println("重连次数过多，不进行重连，即将推出客户端............");
            flag = false;
            System.exit(0);
        }

    }


    /*=======================================================================================================================*/
    public static void main(String[] args) throws Exception {
        NettyClient nettyClient = NettyClient.getInstance("127.0.0.1", 9527).init();


        TimeUnit.SECONDS.sleep(2);
        System.out.println("自己断开连接");
        //关闭时异步的，如果不控制，可能在下面获取futur判断e的时候future还活在，之后才关闭的
        //可以采用close之后加sync控制，或者在while里面判断writeAndFlush的返回值是否是alive的
        nettyClient.getChannelFuture().channel().close();

        ChannelFuture future1 = nettyClient.getChannelFuture().channel().writeAndFlush("lmy");
        while (!future1.channel().isActive()) {
            System.out.println("没有发送成功，200ms之后再重发");
            future1 = nettyClient.getChannelFuture().channel().writeAndFlush("lmy");
            TimeUnit.MILLISECONDS.sleep(200);
        }
        ChannelFuture future = nettyClient.getNewChannelFuture().channel().writeAndFlush("mingyao======");
    }


//        nettyClient.destory();


//        nettyClient.destory();

    /*
     *bootstrap每调用一次connect都是一个新的连接
     * 服务端输出如下:nioEventLoopGroup-3-4 ===>client says: lmy
     *   nioEventLoopGroup-3-4 ===>client says: lmy
     *   nioEventLoopGroup-3-5 ===>client says: lmy
     *   nioEventLoopGroup-3-3 ===>client says: lmy
     *   nioEventLoopGroup-3-1 ===>client says: lmy
     *   nioEventLoopGroup-3-1 ===>client says: lmy
     * */
    //验证了服务端多线程
//        for (int i = 0; i < 10000; i++) {
//            new Thread(() -> {
//                try {
//                    Channel channel = nettyClient.getNewConnect("127.0.0.1", 9527).channel();
//                    channel.writeAndFlush("lmy");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//        for (int i = 0; i < 100000; i++) {
//            new Thread(()->{
//                nettyClient.future.channel().writeAndFlush("刘明瑶");
//            }).start();
//        }
}