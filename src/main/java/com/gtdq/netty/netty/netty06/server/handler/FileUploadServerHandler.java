package com.gtdq.netty.netty.netty06.server.handler;

import com.gtdq.netty.netty.netty06.model.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
/**
 * @author  : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */
public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
	private long byteRead;
    private volatile long start = 0;
    private String file_dir = "E:\\tmp";
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadServerHandler.class);

    /**
     * @author  : LiuMing
     * @date : 2019/8/12 14:24
     * @description :   有新的客户端连接上的时候被触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	super.channelActive(ctx);
        LOGGER.info("服务端：channelActive()");
    }


    /**
     * @author  : LiuMing
     * @date : 2019/8/12 14:28
     * @description :   任意一个连接的客户端断开连接都会触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	// TODO Auto-generated method stub
    	super.channelInactive(ctx);
        LOGGER.info("服务端：channelInactive()");
    	ctx.flush();
    	ctx.close();
    }
    
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("收到客户端发来的文件,正在处理....");
        if (msg instanceof FileUploadFile) {
            FileUploadFile ef = (FileUploadFile) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();//文件名
            String path = file_dir + File.separator + md5;
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");//r: 只读模式 rw:读写模式
            randomAccessFile.seek(start);//移动文件记录指针的位置,
            randomAccessFile.write(bytes);//调用了seek（start）方法，是指把文件的记录指针定位到start字节的位置。也就是说程序将从start字节开始写数据
            start = start + byteRead;
            if (byteRead > 0) {
                LOGGER.info("服务端回应客户端，继续接受剩余数据");
                ctx.writeAndFlush(start);//向客户端发送消息
                randomAccessFile.close();
//                if(byteRead!=1024 * 10){
//                	Thread.sleep(1000);
//                	channelInactive(ctx);
//                }
            } else {
                ctx.close();
            }
            LOGGER.info("处理完毕,文件路径:"+path+","+byteRead);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        LOGGER.info("FileUploadServerHandler--异常");
    }
}
