package com.gtdq.netty.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author : LiuMingyao
 * @date : 2019/8/14 17:23
 * @description : 异常工具类
 */
public class ExceptionUtil {
    /**
     * @author : LiuMing
     * @date : 2019/8/14 17:27
     * @description :   获取完整的堆栈字符串信息
     */
    public static String getExceptionInfo(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter, true));
        return stringWriter.getBuffer().toString();
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/25 11:52
     * @description :  isAdd <code>true</code>该异常信息是否是追加到其他内容会面
     */
    public static String getExceptionInfo(Throwable t, boolean isAdd) {
        String prefix = "\t异常信息如下:\n";
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter, true));
        String s = stringWriter.getBuffer().toString();
        return isAdd ? prefix + s : s;
    }
}