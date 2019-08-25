package com.gtdq.netty.netty;

import com.gtdq.netty.nettyCollections.server.handler.ServerHandler;

import java.io.File;

/**
 * @author : LiuMingyao
 * @date : 2019/8/25 15:20
 * @description : TODO
 */
public class FileTest {
    public static void main(String[] args) throws Exception {
        //传输文件
        File file = new File("e:/temp/test.txt");

        /*
         * deleteOnExit();之后的文件不能使用createNewFile();创建
         *
         * <p> 清空文件操作
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
         * */
        // file.deleteOnExit();
/*        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("");
        fileWriter.flush();
        fileWriter.close();*/

        new ServerHandler("").createParentDir(file);
    }
}