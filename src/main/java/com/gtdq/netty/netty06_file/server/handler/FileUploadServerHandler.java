package com.gtdq.netty.netty06_file.server.handler;

import com.gtdq.netty.netty06_file.model.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */
public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
    private String file_dir = "E:\\tmp";
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadServerHandler.class);

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
     * @description :   任意一个连接的客户端断开连接都会触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        LOGGER.info("客户端{}断开连接", ctx.channel().remoteAddress());
        ctx.flush();
        ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileUploadFile) {
            FileUploadFile ef = (FileUploadFile) msg;
            if (ef.getMsgType() == 1) {
                //代表建立连接的请求

                /**
                 * 之后就是接受客户端发送文件的请求的
                 * <p>这时候需要判断是不是断点续传，在服务器上找到改文件，查看该文件的startPos即字节长度，之后返回给客户端，让客户端从该位置继续传输</p>
                 */
                File currentFile = new File(file_dir+"\\"+ef.getFile_md5());
                if (Objects.nonNull(currentFile) && currentFile.exists()) {
                    ctx.writeAndFlush(currentFile.length());
                } else {
                    //第一次连接，没有传输过文件的情况
                    ctx.writeAndFlush(new Integer(1));
                    LOGGER.info("服务端和客户端建立连接....");
                }
                return;

            }


            LOGGER.info("服务端正在接受客户端发送来的文件....从{}读到{}", ef.getStarPos(), ef.getStarPos() + ef.getByteSize());
            byte[] bytes = ef.getBytes();

            String md5 = ef.getFile_md5();//文件名
            String path = file_dir + File.separator + md5;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");//r: 只读模式 rw:读写模式
            randomAccessFile.seek(ef.getStarPos());//移动文件记录指针的位置,
            randomAccessFile.write(bytes);//调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
            long start = ef.getStarPos() + ef.getByteSize();
            TimeUnit.SECONDS.sleep(2);
            ctx.writeAndFlush(start);//向客户端发送消息
            randomAccessFile.close();
        } else {
            ctx.close();
            LOGGER.info("处理完毕");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        LOGGER.info("FileUploadServerHandler--异常");
    }
}
