package com.gtdq.netty.zerocopy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author : LiuMingyao
 * @date : 2019/11/4 22:50
 * @description : TODO
 */
public class NewIoClient {
    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open();
        //文件4g+
        //String path = "E:\\CiKeXinTiao8AoDeSai\\DataPC_patch_01.forge";
        //270m+
        String path = "E:\\CiKeXinTiao8AoDeSai\\ACOdyssey.exe";
        String p = "C:\\Users\\Liu Mingyao\\Desktop\\testTransto.txt";
        FileChannel outChannel = new FileOutputStream(p).getChannel();
        socketChannel.connect(new InetSocketAddress("localhost", 8899));
        socketChannel.configureBlocking(true);

        FileChannel fileChannel = new FileInputStream(path).getChannel();
        //long currentCount = fileChannel.transferTo(0, size, socketChannel);
        System.out.println("总字节数:" + fileChannel.size());
        long startTime = System.currentTimeMillis();
        long size = fileChannel.size();
        long currentCount = fileChannel.transferTo(0, size, socketChannel);
        long position = 0;
        long total = 0;
        while (position < size) {
            long currentNum = fileChannel.transferTo(position, fileChannel.size(), socketChannel);
            System.out.println("发送：" + currentNum);
            if (currentNum <= 0) {
                break;
            }
            total += currentNum;
            position += currentNum;
        }
        System.out.println("发送总字节数:" + total + "  耗时:" + (System.currentTimeMillis() - startTime));

        fileChannel.close();
        //等待数据全部传输完毕
    }
}