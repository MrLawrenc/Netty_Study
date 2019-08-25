package com.gtdq.netty.nettyCollections.service;

import com.gtdq.netty.nettyCollections.model.FileModel;

import java.io.InputStream;

public interface FileUploadService extends FileService {
    boolean upload(FileModel file);

    boolean upload(InputStream inputStream);
}
