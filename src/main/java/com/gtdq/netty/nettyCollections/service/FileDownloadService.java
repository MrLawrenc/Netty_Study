package com.gtdq.netty.nettyCollections.service;

import com.gtdq.netty.nettyCollections.model.FileModel;

import java.io.InputStream;

public interface FileDownloadService extends FileService {
    void download(FileModel file);

    void download(InputStream inputStream);
}
