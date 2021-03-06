package com.gtdq.netty.netty06_file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:23
 * @description :   TODO
 */
@AllArgsConstructor
@Setter
@Getter
public class FileUploadFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private int msgType;//1->建立连接;2->发送文件
    private File file;// 文件
    private String file_md5;// 文件名
    private long starPos;// 开始位置
    private byte[] bytes;// 文件字节数组
    private long byteSize;// 每次读多少个字节
    private long endPos;// 结尾位置

    /**
     * @param byteSize 每次传输的字节数
     * @author : LiuMing
     * @date : 2019/8/13 13:06
     * @description :   TODO
     */
    public FileUploadFile(File file, long starPos, long byteSize, long endPos) {
        if (Objects.isNull(file)) throw new NullPointerException("file is null");
        this.msgType = 1;
        this.file = file;
        this.file_md5 = this.file.getName();
        this.starPos = starPos;
        this.byteSize = byteSize;
        if (file.length() < endPos)
            throw new IllegalArgumentException("endPos is oversize . Must Be Satisfied : endPos <= file.length() ");
        this.endPos = endPos;
    }

    public FileUploadFile(File file, long starPos, long byteSize) {
        if (Objects.isNull(file)) throw new NullPointerException("file is null");
        this.msgType = 1;
        this.file = file;
        this.file_md5 = this.file.getName();
        this.starPos = starPos;
        this.byteSize = byteSize;
        this.endPos = this.file.length();
    }
}
