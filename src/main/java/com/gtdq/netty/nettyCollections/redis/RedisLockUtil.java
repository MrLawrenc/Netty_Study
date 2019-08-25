package com.gtdq.netty.nettyCollections.redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author : LiuMingyao
 * @date : 2019/8/20 21:31
 * @description : TODO
 */
public class RedisLockUtil {
    private RedisTemplate redisTemplate;
    private String prefix = "lock:";

    public RedisLockUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * @param acquireTimeOut 请求锁的超时时间 单位ms
     * @param lockName       锁的名字
     * @param lockTimeOut    锁本身的过期时间 单位s 防止死锁
     * @return 返回锁的唯一标识;<code>null</code>没有获取到锁
     * @author : LiuMing
     * @date : 2019/8/20 21:35
     * @description :   获取锁
     */
    public String acquireLock(String lockName, long acquireTimeOut, long lockTimeOut) {
        String identifier = UUID.randomUUID().toString();
        String lockKey = prefix + lockName;
        int expireNum = (int) (acquireTimeOut / 10);//大概循环请求锁的次数,肯定<10次
        long endTime = System.currentTimeMillis() + acquireTimeOut;
        while (System.currentTimeMillis() < endTime) {
            if (redisTemplate.opsForValue().setIfAbsent(lockKey, identifier)) {
                Boolean expire = redisTemplate.expire(lockKey, lockTimeOut, TimeUnit.SECONDS);
                if (expire) return identifier;
            }

            Long expire = redisTemplate.getExpire(lockKey);//锁的过期时间
            if (expire == -1) {

            }
            try {
                TimeUnit.MILLISECONDS.sleep(expireNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param lockName   锁的名字
     * @param identifier 锁的唯一标识
     * @author : LiuMing
     * @date : 2019/8/20 22:25
     * @description :   释放锁
     */
    public boolean releaseLock(String lockName, String identifier) {
        String lockKey = prefix + lockName;
        if (identifier.equals(redisTemplate.opsForValue().get(lockKey))) {
            Boolean delete = redisTemplate.delete(lockKey);
            System.out.println(Thread.currentThread().getName()+" 释放锁");
            return delete;
        }
        return false;
    }

    /**
     * @author : LiuMing
     * @date : 2019/8/21 10:11
     * @description :   获取简易锁
     */
    public String acquireSimpleLock(String lockName) {
        String identifier = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(prefix + lockName, identifier);
        return success ? identifier : null;
    }

}