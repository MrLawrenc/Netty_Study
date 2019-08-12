package com.gtdq.netty.netty.netty06.client;

import com.gtdq.netty.netty.netty06.client.work.FileUploadClient;
import com.gtdq.netty.netty.netty06.model.FileUploadFile;

import java.io.File;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:28
 * @description :   TODO
 */
public class ClientFileApp {

    public static void main(String[] args) throws Exception {
        File file = new File("E:\\MyProject\\study_netty\\src\\main\\java\\com\\gtdq\\netty\\netty\\netty06\\client\\liu.txt");

        FileUploadFile fileUploadFile = new FileUploadFile(file,"liu.txt",0,null, 3000);//end-start  就是每次从start读，每次读end-start字节
        new FileUploadClient().connect(9527,"127.0.0.1",fileUploadFile);
    }


}
