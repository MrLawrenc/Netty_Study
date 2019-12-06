package com.gtdq.netty.zerocopy;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/11/4 22:50
 * @description : TODO
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
public class NewIoClient {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        //文件4g+
        //String path = "E:\\CiKeXinTiao8AoDeSai\\DataPC_patch_01.forge";
        //270m+
        String path = "E:\\CiKeXinTiao8AoDeSai\\ACOdyssey.exe";
        String p = "E:\\ggg6.7z";
        socketChannel.connect(new InetSocketAddress("localhost", 8900));
        socketChannel.configureBlocking(true);

        FileChannel fileChannel = new FileInputStream(p).getChannel();
        //long currentCount = fileChannel.transferTo(0, size, socketChannel);
        System.out.println("总字节数:" + fileChannel.size());
        long startTime = System.currentTimeMillis();
        long size = fileChannel.size();
        long position = 0;
        long total = 0;
        while (position < size) {
            /**
             *
             * 具体参加博客:https://blog.csdn.net/qq_40695278/article/details/103421465
             * int icount = (int)Math.min(count, Integer.MAX_VALUE);第一层会有文件大小限制，最大为integer，2147483647  字节---->2048m
             *
             * 在FileChannelImpl里面 会调用transferToTrustedChannel方法(Attempt a mapped transfer, but only to trusted channel types,
             * 使用直接内存映射)，使用直接内存映射， 里面定义了一个成员变量
             * // Maximum size to map when using a mapped buffer
             * private static final long MAPPED_TRANSFER_SIZE = 8L*1024L*1024L;
             * 在transferToTrustedChannel方法会使用下面判断本次操作的字节大小。
             * long size = Math.min(remaining, MAPPED_TRANSFER_SIZE);
             * 这时限制在8,388,608‬字节-->8m
             */
            long currentNum = fileChannel.transferTo(position, fileChannel.size(), socketChannel);
            System.out.println("复制字节数:" + currentNum);
            if (currentNum <= 0) {
                break;
            }
            total += currentNum;
            position += currentNum;
        }
        System.out.println("发送总字节数:" + total + "  耗时:" + (System.currentTimeMillis() - startTime));

        //晚点关闭，查看服务器接收字节
        TimeUnit.SECONDS.sleep(10);
        fileChannel.close();
    }

    public static void restReq() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(5000))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8087/gtbdp/sysDic/list"))
                .timeout(Duration.ofMinutes(2))
//                .header("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofFile(Paths.get("file.json")))
                .build();

        //同步
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        //异步
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println);
    }

}