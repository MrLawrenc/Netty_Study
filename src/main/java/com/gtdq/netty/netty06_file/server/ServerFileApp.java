package com.gtdq.netty.netty06_file.server;


import com.gtdq.netty.netty06_file.server.work.FileUploadServer;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   文件传输(支持断点续传)
 */
public class ServerFileApp {

    public static void main(String[] args) throws Exception {
        new FileUploadServer().bind(9527);
    }
}
