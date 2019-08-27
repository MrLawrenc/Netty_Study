package com.gtdq.netty.nettyCollections.client.handler;

import com.gtdq.netty.nettyCollections.client.work.Client;
import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.util.ExceptionUtil;
import com.gtdq.netty.util.LogUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.RandomAccessFile;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:44
 * @description :   TODO
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private volatile int byteSize;
    private RandomAccessFile randomAccessFile;
    private FileModel fileUploadFile;
    private long fileUploadLen;
    private final static Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);


    private final Client client;
    @Getter
    private ChannelHandlerContext ctx;

    public ClientHandler(Client client) {
        this.client = client;
    }

//    public ClientHandler(FileUploadFile ef, Client client) {
//        this.client = client;
//        File file = ef.getFile();
//        if (Objects.nonNull(file) && file.exists() && file.isFile()) {
//            this.fileUploadFile = ef;
//            this.fileUploadLen = fileUploadFile.getFile().length();
//            this.byteSize = (int) fileUploadFile.getByteSize();//每次读的字节数
//        } else {
//            LOGGER.error("请检查需要传输的文件是否存在,并且是一个文件!");
//        }
//    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 13:54
     * @description :   和服务器断开会触发的方法
     * <p></p>
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (client.isNeedConnection()) {
            //断线重连
            client.doReconnection();
        } else {
            super.channelInactive(ctx);
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 13:55
     * @description :   在Channel注册EventLoop、绑定SocketAddress和连接ChannelFuture的时候都有可能会触发channelActive方法的调用。
     * <p>当前channel激活的时候</p>
     */
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx=ctx;
        LOGGER.info("正在和服务{}端建立连接.....", ctx.channel().remoteAddress());
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:11
     * @description :   当前channel从远端读取到数据的时候会触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {


    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:11
     * @description :   异常会触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogUtil.errorLog("client exception" + ExceptionUtil.getExceptionInfo(cause, true));
        ctx.close();
    }


}