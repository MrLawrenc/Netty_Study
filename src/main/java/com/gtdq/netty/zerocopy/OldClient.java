package com.gtdq.netty.zerocopy;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * @author : LiuMingyao
 * @date : 2019/11/4 22:17
 * @description : 常规io流，从用户态-->内核态-->硬件
 */
public class OldClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 8899);
        //文件4g+
        String path = "E:\\CiKeXinTiao8AoDeSai\\DataPC_patch_01.forge";
        String p = "E:\\ggg6.7z";
        FileInputStream inputStream = new FileInputStream(p);

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        byte[] bytes = new byte[4096];
        long readCount;
        long total = 0;

        long startTime = System.currentTimeMillis();

        while ((readCount = inputStream.read(bytes)) >= 0) {
            System.out.println("写"+readCount+"个字节.....");
            total += readCount;
            dataOutputStream.write(bytes);
        }
        //发送总字节数:1727150186,耗时:9386 zero copy 1563
        System.out.println("发送总字节数:" + total + "," + "耗时:" + (System.currentTimeMillis() - startTime));
        socket.close();
    }
}