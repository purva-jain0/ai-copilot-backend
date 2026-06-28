package com.aicopilot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String OTP_KEY_PREFIX = "OTP:";
    private static final int OTP_EXPIRY_MINUTES = 5;

    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    public boolean validateOtp(String email, String inputOtp) {
        String key = OTP_KEY_PREFIX + email;
        String cachedOtp = redisTemplate.opsForValue().get(key);

        if (cachedOtp != null && cachedOtp.equals(inputOtp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}