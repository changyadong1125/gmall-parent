package com.atguigu.gmall.product.demo;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.atguigu.gmall.product.demo.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class TestServiceImpl implements TestService {

   @Resource
   private StringRedisTemplate stringRedisTemplate;
   @Resource
   private RedissonClient redissonClient;

   @Override
   public void testLock() {
      RLock lock = redissonClient.getLock("lock");
      lock.lock();
      try {
         //查询Redis中的num值
         String value = (String)this.stringRedisTemplate.opsForValue().get("num");
         // 没有该值return
         if (StringUtils.isBlank(value)){
            return ;
         }
         // 有值就转成成int
         int num = Integer.parseInt(value);
         // 把Redis中的num值+1
         this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
      } catch (NumberFormatException e) {
         throw new RuntimeException(e);
      } finally {
         lock.unlock();
      }
   }

   @Override
   public String read() {
      RReadWriteLock rwlock = redissonClient.getReadWriteLock("myLock");
      rwlock.readLock().lock(10,TimeUnit.SECONDS);
      String msg = this.stringRedisTemplate.opsForValue().get("msg");
      rwlock.readLock().unlock();
      return msg;
   }

   @Override
   public void write() {
      RReadWriteLock rwlock = redissonClient.getReadWriteLock("myLock");
      rwlock.writeLock().lock(10, TimeUnit.SECONDS);
      String uuid = UUID.randomUUID().toString();
      this.stringRedisTemplate.opsForValue().set("msg",uuid);
   }
//   @Override
//   public synchronized void testLock() {
////      Boolean aBoolean = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", "atguigu",5, TimeUnit.SECONDS);
//      String token = UUID.randomUUID().toString().replaceAll("-", "");
//      Boolean aBoolean = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", token,5, TimeUnit.SECONDS);
//      if (aBoolean){
//         // 查询Redis中的num值
//         String value = (String)this.stringRedisTemplate.opsForValue().get("num");
//         // 没有该值return
//         if (StringUtils.isBlank(value)){
//            return ;
//         }
//         // 有值就转成成int
//         int num = Integer.parseInt(value);
//         // 把Redis中的num值+1
//         this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
//
//         //锁续期
//         Thread thread = new Thread(() -> {
//            Long ttlCount = this.stringRedisTemplate.getExpire("lock");
//            if (ttlCount != null && ttlCount <= 1) {
//               this.stringRedisTemplate.expire("lock", 3, TimeUnit.SECONDS);
//            }
//         });
//         thread.setDaemon(true);
//
//         //使用lua脚本进行锁删除
//         DefaultRedisScript<Long> defaultRedisScript = new DefaultRedisScript<>();
//         String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
//                 "then\n" +
//                 "    return redis.call(\"del\",KEYS[1])\n" +
//                 "else\n" +
//                 "    return 0\n" +
//                 "end";
//         defaultRedisScript.setScriptText(script);
//         defaultRedisScript.setResultType(Long.class);
//         Long lock = this.stringRedisTemplate.execute(defaultRedisScript, Arrays.asList("lock"), token);
//         if (lock!=null&&lock ==1){
//            System.out.println("成功释放锁");
//         }
//         //第一个参数封装lua脚本的对象  第二个参数是缓存锁的key Arrays。asList
//
//
////         while (token.equals(this.stringRedisTemplate.opsForValue().get("lock"))){
////            this.stringRedisTemplate.delete("lock");
////         }
//
//      }else{
//         try {
//            Thread.sleep(300);
//         } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//         }
//         testLock();
//      }
//   }
}