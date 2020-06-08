package org.linlinjava.litemall.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalUtil {
    private static final Map<String, ThreadLocal<?>> threadLocalMap = new ConcurrentHashMap<>();
    public static <T> ThreadLocal<T> getThreadLocal(String name) {
        return (ThreadLocal<T>) threadLocalMap.computeIfAbsent(name, key -> new TransmittableThreadLocal<>());
    }
}