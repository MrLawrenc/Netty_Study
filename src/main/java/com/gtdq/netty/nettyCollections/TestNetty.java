package com.gtdq.netty.nettyCollections;

import com.gtdq.netty.nettyCollections.client.work.Client;
import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.nettyCollections.server.work.Server;
import com.gtdq.netty.nettyCollections.service.impl.FileUploadServiceImpl;
import com.gtdq.netty.util.LogUtil;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/25 22:57
 * @description : TODO
 */
@Component
public class TestNetty {
    @Autowired
    private FileUploadServiceImpl uploadService;
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;


    public void testAllSend() {
        Server server = null;
        try {
            server = new Server().init(9527);
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.errorLog("服务端启动异常!");
        }

        Client client = Client.getInstance("127.0.0.1", 9527).init();
        uploadService.initClient(client);

        //一次性传输整个文件
        FileModel fileModel = new FileModel(new File("e:/test.txt"));



        //模拟断线，然后让kafka去发送文件
        client.setNeedConnection(true, kafkaTemplate);
        ChannelFuture selfChannelFuture = client.getSelfChannelFuture();
        try {
            TimeUnit.SECONDS.sleep(3);
            System.out.println("================");
            client.closeClient();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


       /* boolean isSuccess = uploadService.upload(fileModel);
        LogUtil.infoLog("全量传输文件结果:{}", isSuccess);

        //指定传输从start-->end的字节
        int start = 10;
        int end = 100;
        FileModel fileModel1 = new FileModel(new File(""), start, end);
        boolean b = uploadService.continueTransport(fileModel1);
        LogUtil.infoLog("增量传输文件结果:{}", isSuccess);
        try {
            TimeUnit.SECONDS.sleep(3);
            if (server != null) {
                server.closeServer();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    public void testContinueSend() {
        Server server = null;
        try {
            server = new Server().init(9527);
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.errorLog("服务端启动异常!");
        }

        Client client = Client.getInstance("127.0.0.1", 9527).init();
        uploadService.initClient(client);

        //一次性传输整个文件
//        FileModel fileModel = new FileModel(new File("e:/test.txt"), 0, 20);
        FileModel fileModel = new FileModel(new File("e:/test.txt"), 20, 2000000);
        boolean isSuccess = uploadService.continueTransport(fileModel);
        LogUtil.infoLog("增量传输文件结果:{}", isSuccess);


        client.setNeedConnection(true, kafkaTemplate);
        client.closeClient();

        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}