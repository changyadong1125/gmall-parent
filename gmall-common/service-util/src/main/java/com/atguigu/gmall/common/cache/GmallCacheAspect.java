package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author atguigu-mqx
 */
@Component
@Aspect
@Slf4j
public class GmallCacheAspect {                            

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:使用通知进行切注解
     * 切点是注解GmallCache
     * 切面试是 服务层中的方法
     *
     */

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheAspectMethod(ProceedingJoinPoint point){
       //创建一个对象
        Object object = null;
       //实现分布式锁的业务逻辑
            //判断缓存中是否有这个数据，有数据直接返回 没有数据 加锁 去数据库中查询 放入到redis 释放锁
            //先组成缓存的key 获取注解的前缀和后缀属性以及方法的参数
            //需要首先获取注解  getSignature获取切点的签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
            //获取前后缀
            String suffix = gmallCache.suffix();
            String prefix = gmallCache.prefix();
            //获取参数
            Object[] args = point.getArgs();
            String key = prefix+Arrays.asList(args)+suffix;
        try {
            //从缓存中获取数据进行判断
            object = this.redisTemplate.opsForValue().get(key);
            //为空说明缓存没有数据
            if (null == object){
                //定义一个LocKey
                String locKey  = prefix+Arrays.asList(args)+":lock";
                RLock lock = this.redissonClient.getLock(locKey);
                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                if (result){
                    //获取到锁
                    try {
                        //查询数据库 {查询带有注解的方法体}放入缓存
                        object = point.proceed(args);
                        //判断是否查询到数据
                        if (null==object){
                            Object o = new Object();
                            this.redisTemplate.opsForValue().set(key,o,RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return o;
                        }
                        //数据库中有数据
                        this.redisTemplate.opsForValue().set(key,object,RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return object;
                    } finally {
                        lock.unlock();
                    }

                }else{
                    Thread.sleep(100);
                    return this.gmallCacheAspectMethod(point);
                }

            }else{
                return object;
            }
        } catch (Throwable e) {
            log.error("发生异常：{}",e.getMessage());
            e.printStackTrace();
        }
//发生异常直接从数据库中查询
        try {
            return point.proceed(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }





















//    @SneakyThrows
//    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
//    public Object gmallCacheAspectMethod1(ProceedingJoinPoint point){
//        //  定义一个对象
//        Object obj = new Object();
//        /*
//         业务逻辑！
//         1. 必须先知道这个注解在哪些方法 || 必须要获取到方法上的注解
//         2. 获取到注解上的前缀
//         3. 必须要组成一个缓存的key！
//         4. 可以通过这个key 获取缓存的数据
//            true:
//                直接返回！
//            false:
//                分布式锁业务逻辑！
//         */
//        MethodSignature methodSignature = (MethodSignature) point.getSignature();
//        GmallCache gmallCache = methodSignature.getMethod().getAnnotation(GmallCache.class);
//        //   获取到注解上的前缀
//        String prefix = gmallCache.prefix();
//        //  组成缓存的key！ 获取方法传递的参数
//        String key = prefix+ Arrays.asList(point.getArgs()).toString();
//        try {
//            //  可以通过这个key 获取缓存的数据
//            obj = this.getRedisData(key,methodSignature);
//            if (obj==null){
//                //  分布式业务逻辑
//                //  设置分布式锁，进入数据库进行查询数据！
//                RLock lock = redissonClient.getLock(key + ":lock");
//                //  调用trylock方法
//                boolean result = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
//                //  判断
//                if(result){
//                    try {
//                        //  执行业务逻辑：直接从数据库获取数据
//                        //  这个注解 @GmallCache 有可能在 BaseCategoryView getCategoryName , List<SpuSaleAttr> getSpuSaleAttrListById ....
//                        obj = point.proceed(point.getArgs());
//                        //  防止缓存穿透
//                        if (obj==null){
//                            Object object = new Object();
//                            //  将缓存的数据变为 Json 的 字符串
//                            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(object),RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
//                            return object;
//                        }
//                        //  将缓存的数据变为 Json 的 字符串
//                        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(obj),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
//                        return obj;
//                    }finally {
//                        //  解锁
//                        lock.unlock();
//                    }
//                }else {
//                    //  没有获取到
//                    try {
//                        Thread.sleep(100);
//                        return gmallCacheAspectMethod(point);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }else {
//                //  直接从缓存获取的数据！
//                return obj;
//            }
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        //  数据库兜底！
//        return point.proceed(point.getArgs());
//    }
//
//    /**
//     * 从缓存中获取数据！
//     * @param key
//     * @return
//     */
//    private Object getRedisData(String key,MethodSignature methodSignature) {
//        //  在向缓存存储数据的时候，将数据变为Json 字符串了！
//        //  通过这个key 获取到缓存的value
//        String strJson = (String) this.redisTemplate.opsForValue().get(key);
//        //  判断
//        if(!StringUtils.isEmpty(strJson)){
//            //  将字符串转换为对应的数据类型！
//            return JSON.parseObject(strJson,methodSignature.getReturnType());
//        }
//        return null;
//    }

}
