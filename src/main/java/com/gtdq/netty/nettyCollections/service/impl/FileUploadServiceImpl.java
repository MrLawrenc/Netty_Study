package com.gtdq.netty.nettyCollections.service.impl;

import com.alibaba.fastjson.JSON;
import com.gtdq.netty.nettyCollections.client.work.Client;
import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.nettyCollections.model.MsgType;
import com.gtdq.netty.nettyCollections.redis.RedisUtil;
import com.gtdq.netty.nettyCollections.service.FileUploadService;
import com.gtdq.netty.util.ExceptionUtil;
import com.gtdq.netty.util.LogUtil;
import com.gtdq.netty.util.ParamUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/24 18:05
 * @description : TODO
 */
@Component
public class FileUploadServiceImpl implements FileUploadService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaTemplate<String, List> kafkalist;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisUtil redisUtil;

    private static Client client;


    public FileUploadServiceImpl initClient(Client client) {
        if (null == client) throw new NullPointerException("client must not be null");
        this.client = client;
        return this;
    }

    @Override
    public boolean upload(FileModel fileModel) {
        return transport(fileModel);
    }

    @Override
    public boolean upload(InputStream inputStream) {
        byte[] bytes;
        FileModel fileModel = null;
        try (inputStream) {
            bytes = inputStream.readAllBytes();
            fileModel = new FileModel(bytes);
            ChannelFuture future = client.getSelfChannelFuture().channel().writeAndFlush(fileModel);
            if (future.channel().isActive()) return true;
        } catch (IOException e) {
            LogUtil.errorLog("inputStream.readAllBytes()出错" + ExceptionUtil.getExceptionInfo(e, true));
        }
        //todo 连接已经断开,发送失败 记录到kafka/redis
        LogUtil.errorLog("本次数据传输失败，即将记录到kafka/redis");
        redisUtil.lLeftPush("sendFail:1", fileModel);
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
//                int a=1/0;//模拟出错就，记录到redis
                ChannelFuture future = channelFuture.channel().writeAndFlush(fileModel);//发送消息到服务端
                if (future.channel().isActive()) return true;
            }
        } catch (FileNotFoundException e) {
            LogUtil.errorLog("获取RandomAccessFile出错" + ExceptionUtil.getExceptionInfo(e, true));
        } catch (IOException e) {
            LogUtil.errorLog("raf.read(bytes)出错" + ExceptionUtil.getExceptionInfo(e, true));
        } catch (Exception e) {
            LogUtil.errorLog("upload出错" + ExceptionUtil.getExceptionInfo(e, true));
        }
        //todo 连接已经断开,发送失败 记录到kafka/redis
        LogUtil.errorLog("本次数据传输失败，即将记录到kafka/redis,message:{}", fileModel);
        redisUtil.lLeftPush("sendFail:1", JSON.toJSONString(fileModel));
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
        } catch (Exception e) {
            LogUtil.errorLog("continueTransport异常" + ExceptionUtil.getExceptionInfo(e, true));
        }
        LogUtil.errorLog("本次数据传输失败,即将记录到kafka/redis,message:{}", fileModel);
        redisUtil.lLeftPush("sendFail:2", fileModel);
        return false;

    }

    /**
     * @author : LiuMing
     * @date : 2019/8/26 10:37
     * @description :   redis存入失败数据规则:sendFail为前缀，第二个数字为传输文件类型,<code>1</code>全量;<code>2</code>增量
     */
    @KafkaListener(topics = "sendFile")
    public void receive(ConsumerRecord<String, String> consumer) {
        LogUtil.infoLog("{} - {}:  value:{}", consumer.topic(), consumer.key(), consumer.value());
        /**
         * 1.连接可用了(连接断开重连成功会触发)
         * 2.定时任务会隔一段时间发送ConnectionAvailable-->true的消息，触发kafka消费之前二次重发失败的数据
         * */
        if (consumer.key().equals("ConnectionAvailable") && consumer.value().equals("true")) {


            while (true) {
                LogUtil.warnLog("kafka收到了重发全量文件的消息，开始处理全量发送失败的数据...................");
                //todo 拿到redis里面存取的全量发送失败的数据
                FileModel fileModel = JSON.parseObject(String.valueOf(redisUtil.lRightPop("sendFail:1")), FileModel.class);
                if (null == fileModel) break;
                boolean upload = upload(fileModel);
                if (!upload) {
                    LogUtil.warnLog("kafka收到了重发全量文件的消息，但是再次发送失败，已经将数据记录到redis!");
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                LogUtil.warnLog("kafka收到了重发增量文件的消息，开始处理增量发送失败的数据...................");
                //todo 拿到redis里面存取的全量发送失败的数据
                FileModel continueFileModel = JSON.parseObject(String.valueOf(redisUtil.lRightPop("sendFail:2")), FileModel.class);
                if (null == continueFileModel) break;
                boolean upload1 = continueTransport(continueFileModel);
                if (!upload1) {
                    LogUtil.warnLog("kafka收到了重发增量文件的消息，但是再次发送失败，已经将数据记录到redis!");
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LogUtil.infoLog("kafka处理消息完毕...........................");
        }

    }

    /*public void test() {
        ListenableFuture<SendResult<String, String>> send = kafkaTemplate.send("sendFile", "ConnectionAvailable", "true");
        ListenableFuture<SendResult<String, String>> send1 = kafkaTemplate.send("sendFile", "continueUpload", "mydata2");


        send.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("失败");
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                System.out.println("成功");
            }
        });
    }*/
}