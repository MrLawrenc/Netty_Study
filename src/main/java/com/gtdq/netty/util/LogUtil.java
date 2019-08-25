package com.gtdq.netty.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : LiuMingyao
 * @date : 2019/3/16 13:55
 * @description : 日志封装类
 * <p>依赖slf4j</p>
 */
public final class LogUtil {
    private LogUtil() {
    }

    private static Logger getLogger(Class clz) {
        return LoggerFactory.getLogger(clz);
    }

    private static Logger getLogger(String clzName) {
        return LoggerFactory.getLogger(clzName);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/19 11:25
     * @description :   info日志
     */
    public static void infoLog(String str, Object... messages) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;
        String clzName = stackTrace[2].getClassName();
        getLogger(clzName).info(str, messages);
    }


    /**
     * @author : LiuMing
     * @date : 2019/8/19 13:37
     * @description :   error日志
     */
    public static void errorLog(String str, Object... messages) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;
        String clzName = stackTrace[2].getClassName();
        getLogger(clzName).error(str, messages);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/19 13:42
     * @description :   带有异常信息的error日志
     */
    public static void errorLog(String str, Throwable t, Object... messages) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;
        String clzName = stackTrace[2].getClassName();
        getLogger(clzName).error(str + " 异常信息如下:\n" + ExceptionUtil.getExceptionInfo(t), messages);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/19 13:53
     * @description :   带有异常信息的warn日志
     */
    public static void warnLog(String str, Throwable t, Object... messages) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;
        String clzName = stackTrace[2].getClassName();
        getLogger(clzName).warn(str + " 异常信息如下:\n" + ExceptionUtil.getExceptionInfo(t), messages);
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/19 13:53
     * @description :   warn日志
     */
    public static void warnLog(String str, Object... messages) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) return;
        String clzName = stackTrace[2].getClassName();
        getLogger(clzName).warn(str, messages);
    }

    /**
     * @param params 需要拼接的参数
     * @date : 2019/3/19 13:53
     * @author Liu Ming
     * 记录普通的日志信息
     */
    @Deprecated
    public void infoLog(Class clz, String str, Object... params) {
        getLogger(clz).warn(str, params);
    }

    /**
     * @author : Liu Ming
     * @date : 2019/3/19 13:53
     * 记录异常日志信息
     */
    @Deprecated
    public void exceptionLog(Class clz, String str, Object... params) {
        getLogger(clz).warn(str, params);
    }

}