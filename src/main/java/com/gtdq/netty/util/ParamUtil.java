package com.gtdq.netty.util;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author : LiuMingyao
 * @date : 2019/4/30 13:37
 * @description : TODO
 */
public class ParamUtil {
    /**
     * @param clz        方法所在的类
     * @param methodName 方法名称
     * @author : LiuMing
     * @date : 2019/4/30 13:39
     * @description :获取方法所有参数的名称
     */
    public static String[] getParamNames(Class clz, String methodName) {
        Parameter[] parameters;
        String paramNames[] = new String[10];
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                parameters = method.getParameters();
                if (parameters.length == 0) return null;//无参数情况下长度为0
                LocalVariableTableParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
                paramNames = pnd.getParameterNames(method);
            }
        }
        return paramNames;
    }
}