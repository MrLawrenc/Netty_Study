package com.gtdq.netty.zerocopy;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author : LiuMingyao
 * @date : 2019/11/4 22:45
 * @description : TODO
 */
public class NewIoServer {
    public static void main(String[] args) throws Exception {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(8900);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        ServerSocket serverSocket = serverSocketChannel.socket();
        //即使在timeout，也可以连接socket
        serverSocket.setReuseAddress(true);
        serverSocket.bind(inetSocketAddress);


        ByteBuffer allocate = ByteBuffer.allocate(4096);

        while (true) {
            SocketChannel channel = serverSocketChannel.accept();
            channel.configureBlocking(true);
            System.out.println("连接上服务器.............");
            int totalCount = 0;
            int readCount ;
            while (-1 != (readCount = channel.read(allocate))) {
                totalCount += readCount;
                System.out.println("服务器读到" + totalCount + "字节");
                //丢弃掉数据，重置，重新读到allocate
                allocate.rewind();
            }
            System.out.println("总接收到字节:" + totalCount);
        }
    }
}