package com.gtdq.netty.nettyCollections.service.impl;

import com.gtdq.netty.nettyCollections.model.FileModel;
import com.gtdq.netty.nettyCollections.service.FileDownloadService;

import java.io.InputStream;

/**
 * @author : LiuMingyao
 * @date : 2019/8/24 18:07
 * @description : TODO
 */
public class FileDownloadServiceImpl implements FileDownloadService {
    @Override
    public void download(FileModel file) {
        transport(file);
    }

    @Override
    public void download(InputStream inputStream) {

    }

    @Override
    public boolean transport(FileModel file) {
        return false;
    }

    @Override
    public boolean continueTransport(FileModel file) {
        return false;
    }
}