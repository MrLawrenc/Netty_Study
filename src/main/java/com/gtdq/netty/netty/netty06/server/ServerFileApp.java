package com.gtdq.netty.netty.netty06.server;

import com.gtdq.netty.netty.netty06.server.work.FileUploadServer;

/**
 * @author  : LiuMing
 * @date : 2019/8/12 13:30
 * @description :   TODO
 */
public class ServerFileApp {

    public static void main(String[] args) throws Exception{
        new FileUploadServer().bind(9527);
    }
}
