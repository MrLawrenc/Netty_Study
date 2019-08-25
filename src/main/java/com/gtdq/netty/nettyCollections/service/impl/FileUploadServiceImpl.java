package com.gtdq.netty.nettyCollections.service.impl;

import com.gtdq.netty.nettyCollections.client.work.Client;
import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.nettyCollections.model.MsgType;
import com.gtdq.netty.nettyCollections.service.FileUploadService;
import com.gtdq.netty.util.ExceptionUtil;
import com.gtdq.netty.util.LogUtil;
import com.gtdq.netty.util.ParamUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * @author : LiuMingyao
 * @date : 2019/8/24 18:05
 * @description : TODO
 */
public class FileUploadServiceImpl implements FileUploadService {
    private Client client;

    public FileUploadServiceImpl(Client client) {
        if (null == client) throw new NullPointerException("client must not be null");
        this.client = client;
    }

    @Override
    public boolean upload(FileModel file) {
        return transport(file);
    }

    @Override
    public boolean upload(InputStream inputStream) {
        return false;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/24 18:26
     * @description :   传输FileModel对象(全量)
     */
    @Override
    public boolean transport(FileModel fileModel) {
        fileModel.setMsgType(MsgType.TANSFILE);
        try (RandomAccessFile raf = new RandomAccessFile(fileModel.getFile(), "r");) {
            ChannelFuture channelFuture = client.getSelfChannelFuture();
            raf.seek(fileModel.getStartPos());
            int byteSize = fileModel.getByteSize();//每次读取多少字节
            byte[] bytes = new byte[byteSize];//表示以多少个字节为一组开始读取
            if (raf.read(bytes) != -1) {
                LogUtil.infoLog("本次客户端读取了{}字节的文件，准备发往服务端", byteSize);
                fileModel.setBytes(bytes);
                ChannelFuture future = channelFuture.channel().writeAndFlush(fileModel);//发送消息到服务端
                if (future.channel().isActive()) return true;
                //todo 连接已经断开,发送失败 记录到kafka/redis
            }
        } catch (
                InterruptedException e) {
            LogUtil.errorLog("获取client出错" + ExceptionUtil.getExceptionInfo(e));
        } catch (
                FileNotFoundException e) {
            LogUtil.errorLog("获取RandomAccessFile出错" + ExceptionUtil.getExceptionInfo(e));
        } catch (
                IOException e) {
            LogUtil.errorLog("raf.read(bytes)出错" + ExceptionUtil.getExceptionInfo(e));
        }
        LogUtil.errorLog("本次数据传输失败，即将记录到kafka/redis,message:{}", fileModel);
        return false;


    }

    /**
     * @author : LiuMing
     * @date : 2019/8/25 11:41
     * @description :   续传
     */
    @Override
    public boolean continueTransport(FileModel fileModel) {
        if (fileModel == null) {
            String[] paramNames = ParamUtil.getParamNames(getClass(), "continueTransport");
            throw new NullPointerException(paramNames[0] + " is null");
        }
        fileModel.setMsgType(MsgType.CONTINUETRANS);
        long start = fileModel.getStartPos();
        LogUtil.infoLog("继续传输从第{}字节到第{}字节的数据", start, fileModel.getEndPos());
        /* try (FileInputStream d = new FileInputStream(new File(""))) {  //try-source

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        try {
            RandomAccessFile raf = new RandomAccessFile(fileModel.getFile(), "r");
            raf.seek(start); //将文件定位到start
            int byteSize = fileModel.getByteSize();
            Channel channel = client.getSelfChannelFuture().channel();

            byte[] bytes = new byte[byteSize];//表示以多少个字节为一组开始读取
            if (raf.read(bytes) != -1) {
                fileModel.setBytes(bytes);
                ChannelFuture future = channel.writeAndFlush(fileModel);//发送消息到服务端
                if (future.channel().isActive()) return true;
            }
        } catch (IOException e) {
            LogUtil.errorLog("获取RandomAccessFile异常" + ExceptionUtil.getExceptionInfo(e, true));
        } catch (InterruptedException e) {
            LogUtil.errorLog("获取ChannelFuture异常" + ExceptionUtil.getExceptionInfo(e, true));
        }
        LogUtil.errorLog("本次数据传输失败,即将记录到kafka/redis,message:{}", fileModel);
        return false;

    }
}