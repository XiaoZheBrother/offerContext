package com.campus.recruitment.service;

import com.campus.recruitment.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 同邮箱：1次/分钟，10次/天
    private static final int EMAIL_PER_MINUTE = 1;
    private static final int EMAIL_PER_DAY = 10;

    // 同IP：5次/分钟，50次/天
    private static final int IP_PER_MINUTE = 5;
    private static final int IP_PER_DAY = 50;

    /**
     * 检查频率限制，超限抛出429异常
     */
    public void checkRateLimit(String email, String clientIp) {
        // 邮箱每分钟限制
        checkLimit("rate:email:" + email, EMAIL_PER_MINUTE, 1, TimeUnit.MINUTES,
                "发送过于频繁，请1分钟后再试");

        // 邮箱每天限制
        checkLimit("rate:email:daily:" + email, EMAIL_PER_DAY, 1, TimeUnit.DAYS,
                "今日发送次数已达上限，请明天再试");

        // IP每分钟限制
        checkLimit("rate:ip:" + clientIp, IP_PER_MINUTE, 1, TimeUnit.MINUTES,
                "发送过于频繁，请1分钟后再试");

        // IP每天限制
        checkLimit("rate:ip:daily:" + clientIp, IP_PER_DAY, 1, TimeUnit.DAYS,
                "今日发送次数已达上限，请明天再试");
    }

    private void checkLimit(String key, int maxCount, long timeout, TimeUnit unit, String errorMessage) {
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= maxCount) {
            throw new BusinessException(429, errorMessage);
        }

        if (count == 0) {
            redisTemplate.opsForValue().set(key, "1", timeout, unit);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }
}
