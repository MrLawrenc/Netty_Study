package com.gtdq.netty.netty06_file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.Serializable;

/**
 * @author  : LiuMing
 * @date : 2019/8/12 13:23
 * @description :   TODO
 */
@AllArgsConstructor@Setter@Getter
public class FileUploadFile implements Serializable {

    private static final long serialVersionUID = 1L;
    private File file;// 文件
    private String file_md5;// 文件名
    private long starPos;// 开始位置
    private byte[] bytes;// 文件字节数组
    private long endPos;// 结尾位置

}
