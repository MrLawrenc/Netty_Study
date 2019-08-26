package com.gtdq.netty.nettyCollections.server.handler;

import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.nettyCollections.model.MsgType;
import com.gtdq.netty.util.ExceptionUtil;
import com.gtdq.netty.util.LogUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.RandomAccessFile;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */
@ChannelHandler.Sharable//多个client连接server共享的handler
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    private String filePath;

    public ServerHandler(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:24
     * @description :   有新的客户端连接上的时候被触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        LOGGER.info("客户端{}连接上服务器", ctx.channel().remoteAddress());
    }


    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:28
     * @description :   任意一个连接的客户端channel(服务端和客户端的channel是同一个，只要在任意端断开)断开连接或者服务端异常都会触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("与客户端{}断开连接", ctx.channel().remoteAddress());
        ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileModel) {
            FileModel fileModel = (FileModel) msg;
            MsgType msgType = fileModel.getMsgType();
            //枚举源码equals也是用的=比较的
            if (msgType == MsgType.TANSFILE) { //传输文件
                File file = new File(filePath + "/" + fileModel.getFile_md5());
                //存在就清空
                if (file.exists() && file.length() > 0) {
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write("");
                        fileWriter.flush();
                        //fileWriter.close();会自动释放try里面的资源
                    } catch (Exception e) {
                        LogUtil.errorLog("文件{}已存在，清空内容异常" + ExceptionUtil.getExceptionInfo(e, true), file.getName());
                        ctx.writeAndFlush(MsgType.RECEIVEFALSE);
                    }
                } else {
                    createParentDir(file);//父目录没有就创建
                    file.createNewFile();
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(fileModel.getBytes());
                        ctx.writeAndFlush(MsgType.RECEIVESUCCESS);
                        LogUtil.infoLog("服务端保存文件{}成功，path:{}", file.getName(), file.getPath());
                    } catch (Exception e) {
                        LogUtil.errorLog("文件{}不存在，写入内容发生异常" + ExceptionUtil.getExceptionInfo(e, true), file.getName());
                        ctx.writeAndFlush(MsgType.RECEIVEFALSE);
                    }

                }
            } else if (msgType == MsgType.CONTINUETRANS) { //继续传输请求
                File file = new File(filePath + "/" + fileModel.getFile_md5());
                if (file.exists()) {
                    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {// ;r: 只读模式 rw:读写模式
                        randomAccessFile.seek(fileModel.getStartPos());//移动文件记录指针的位置,
                        randomAccessFile.write(fileModel.getBytes());//调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
                        ctx.writeAndFlush(MsgType.RECEIVESUCCESS);
                    } catch (Exception e) {
                        LogUtil.errorLog("文件{} 写入内容发生异常" + ExceptionUtil.getExceptionInfo(e, true), file.getName());
                        ctx.writeAndFlush(MsgType.RECEIVEFALSE);
                    }
                } else {
                    LogUtil.errorLog("文件{}不存在,无法追加内容", file.getName());
                    ctx.writeAndFlush(MsgType.RECEIVEFALSE);
                }
            } else if (msg == MsgType.CONNECTION) {//建立连接请求
                LogUtil.infoLog("建立连接请求");
            }

        } else {
            LogUtil.infoLog("非FileModel类型，不处理");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.info("ServerHandler异常" + ExceptionUtil.getExceptionInfo(cause, true));
        ctx.close();
    }

    public void createParentDir(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdir();
            createParentDir(parentFile);
        }
    }
}
