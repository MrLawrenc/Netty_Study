package com.gtdq.netty.netty.netty06_file.client.handler;

import com.gtdq.netty.netty.netty06_file.model.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:44
 * @description :   TODO
 */
public class FileUploadClientHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile long start = 0;
    private volatile int lastLength = 0;
    private RandomAccessFile randomAccessFile;
    private FileUploadFile fileUploadFile;
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadClientHandler.class);

    public FileUploadClientHandler(FileUploadFile ef) {
        File file = ef.getFile();
        if (Objects.nonNull(file) && file.exists() && file.isFile()) {
            this.fileUploadFile = ef;
        } else {
            LOGGER.error("请检查需要传输的文件是否存在,并且是一个文件!");
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 13:54
     * @description :   和服务器断开会触发的方法
     * <p></p>
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        LOGGER.info("客户端结束传递文件channelInactive()");
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 13:55
     * @description :   在Channel注册EventLoop、绑定SocketAddress和连接ChannelFuture的时候都有可能会触发channelActive方法的调用。
     * <p>当前channel激活的时候</p>
     */
    public void channelActive(ChannelHandlerContext ctx) {
        LOGGER.info("正在执行channelActive()方法.....");
        LOGGER.info("文件长度....." + fileUploadFile.getFile().length());
        try {
            randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");//只读模式

            randomAccessFile.seek(fileUploadFile.getStarPos());

            lastLength = (int) (fileUploadFile.getEndPos() - fileUploadFile.getStarPos());//每次读的字节数

            byte[] bytes = new byte[lastLength];//表示以多少个字节为一组开始读取
            if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                fileUploadFile.setEndPos(fileUploadFile.getEndPos());
                fileUploadFile.setBytes(bytes);
                ctx.writeAndFlush(fileUploadFile);//发送消息到服务端
            } else {
            }
            LOGGER.info("channelActive()文件已经读完 " + byteRead);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
        LOGGER.info("channelActive()方法执行结束");
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:11
     * @description :   当前channel从远端读取到数据的时候会触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("服务端回应" + msg + "继续读取数据给服务端");
        if (msg instanceof Long) {
            start = Integer.valueOf(((Long) msg).intValue());
            if (start != -1 && start < fileUploadFile.getFile().length()) {
                randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                randomAccessFile.seek(start); //将文件定位到start
                LOGGER.info("剩余长度：" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);

                byte[] bytes = new byte[a];
                if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                    LOGGER.info("byteRead = " + byteRead);
                    fileUploadFile.setEndPos(byteRead + start);
                    fileUploadFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(fileUploadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    ctx.close();
                    LOGGER.info("文件已经读完channelRead()--------" + byteRead);
                }
            }else {
                LOGGER.info("数据发送完毕");
                ctx.close();
            }
        }
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/12 14:11
     * @description :   异常会触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }


}
