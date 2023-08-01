package com.atguigu.gmall.activity.util;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统缓存类
 */
public class CacheHelper {

    /**
     * 缓存容器
     */
    private final static Map<String, Object> cacheMap = new ConcurrentHashMap<>();

    /**
     * 加入缓存
     */
    public static void put(String key, Object cacheObject) {
        cacheMap.put(key, cacheObject);
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        return cacheMap.get(key);
    }

    /**
     * 清除缓存
     *
     * @param key
     * @return
     */
    public static void remove(String key) {
        cacheMap.remove(key);
    }

    public static synchronized void removeAll() {
        cacheMap.clear();
    }
}
