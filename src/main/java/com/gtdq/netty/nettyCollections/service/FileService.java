package com.gtdq.netty.nettyCollections.service;

import com.gtdq.netty.nettyCollections.model.FileModel;

public interface FileService {
    boolean transport(FileModel file);

    boolean continueTransport(FileModel file);
}
