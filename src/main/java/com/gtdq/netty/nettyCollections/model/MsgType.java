package com.gtdq.netty.nettyCollections.model;

/**
 * @author : LiuMingyao
 * @date : 2019/8/25 15:41
 * @description :
 * <<code>1</code>建立连接:
 * <code>2</code>传输文件(全量);
 * <code>3</code>文件续传，传输指定的字节文件;
 * <code>4</code>服务端成功接收;
 * <code>6</code>服务端成功接收，并且需要客户端继续传输剩余部分
 * <code>5</code>服务单端接收失败
 * <code></code>客户端分片的最后一次传输
 */
public enum MsgType {
    CONNECTION(1), TANSFILE(2), CONTINUETRANS(3), RECEIVESUCCESS(4), RECEIVEFALSE(5),CLIENTCONTINUE(6),CLIENTCONTINUE_LAST(7);
    private final int i;

    MsgType(int i) {
        this.i = i;
    }
}