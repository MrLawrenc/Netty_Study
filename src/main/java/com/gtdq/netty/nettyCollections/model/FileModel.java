package com.gtdq.netty.nettyCollections.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author : LiuMing
 * @date : 2019/8/12 13:23
 * @description :   TODO
 */
@AllArgsConstructor
@Getter
@ToString
@Setter
public class FileModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private MsgType msgType;//1->建立连接;2->发送文件;3->文件续传
    private File file;// 文件
    private String file_md5;// 文件名
    private long startPos;// 字节开始位置,从0开始
    private byte[] bytes;// 文件字节数组
    private int byteSize;// 每次读多少个字节
    private long endPos;// 字节结尾位置，到end结束，不包含end

    /**
     * @author : LiuMing
     * @date : 2019/8/25 13:21
     * @description :   指定传输从<code>startPos</code>到<code>endPos</code>的字节
     */
    public FileModel(File file, long startPos, long endPos) {
        if (Objects.isNull(file)) throw new NullPointerException("File cannot be null");
        if (startPos >= endPos) throw new IllegalArgumentException("startPos must be < endPos");
        if (file.length() < endPos) {
            this.endPos = file.length();
        } else {
            this.endPos = endPos;
        }
        this.file = file;
        this.file_md5 = this.file.getName();
        this.startPos = startPos;
        if (this.endPos - this.startPos > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Each transmission must be less than 1G");
        this.byteSize = (int) (this.endPos - this.startPos);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/25 13:26
     * @description :   一次性传输整个文件
     */
    public FileModel(File file) {
        if (Objects.isNull(file)) throw new NullPointerException("File cannot be null");
        this.msgType = MsgType.CONTINUETRANS;
        this.file = file;
        this.file_md5 = this.file.getName();
        if (file.length() > Integer.MAX_VALUE) throw new IllegalArgumentException("The maximum file size is 1G");
        this.byteSize = (int) file.length();
        this.endPos = this.file.length();
    }

}
