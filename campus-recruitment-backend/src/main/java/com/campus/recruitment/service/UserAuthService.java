package com.campus.recruitment.service;

import com.campus.recruitment.dto.request.SendMagicLinkRequest;
import com.campus.recruitment.dto.response.AuthResponse;
import com.campus.recruitment.dto.response.SendMagicLinkResponse;
import com.campus.recruitment.dto.response.UserResponse;
import com.campus.recruitment.entity.User;
import com.campus.recruitment.exception.BusinessException;
import com.campus.recruitment.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
public class UserAuthService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final RateLimitService rateLimitService;
    private final SecretKey userSecretKey;
    private final long userJwtExpiration;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public UserAuthService(UserRepository userRepository,
                           StringRedisTemplate redisTemplate,
                           RateLimitService rateLimitService,
                           @Value("${jwt.user-secret}") String userJwtSecret,
                           @Value("${jwt.user-expiration}") long userJwtExpiration) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.rateLimitService = rateLimitService;
        this.userSecretKey = Keys.hmacShaKeyFor(userJwtSecret.getBytes(StandardCharsets.UTF_8));
        this.userJwtExpiration = userJwtExpiration;
    }

    /**
     * 发送魔法链接（开发模式直接返回token）
     */
    public SendMagicLinkResponse sendMagicLink(SendMagicLinkRequest request, String clientIp) {
        String email = request.getEmail().toLowerCase().trim();

        // 频率限制检查
        rateLimitService.checkRateLimit(email, clientIp);

        // 生成32字符随机token
        byte[] tokenBytes = new byte[16];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = HexFormat.of().formatHex(tokenBytes);

        // 存入Redis，15分钟有效
        redisTemplate.opsForValue().set(
                "magic_link:" + token,
                email,
                15,
                TimeUnit.MINUTES
        );

        // 开发模式：直接返回token；生产环境：发送邮件
        return new SendMagicLinkResponse("已发送登录链接到您的邮箱", token);
    }

    /**
     * 验证魔法链接并登录
     */
    @Transactional
    public AuthResponse verifyMagicLink(String token) {
        String redisKey = "magic_link:" + token;
        String email = redisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            throw new BusinessException("登录链接无效或已过期，请重新发送");
        }

        // 一次性使用：删除Redis key
        redisTemplate.delete(redisKey);

        // 查找或创建用户
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email));

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 生成JWT
        String jwt = generateUserToken(user);

        return new AuthResponse(jwt, toUserResponse(user));
    }

    /**
     * 获取当前用户信息
     */
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toUserResponse(user);
    }

    /**
     * 验证用户JWT Token，返回claims中的userId
     */
    public Long validateUserToken(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(userSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            String role = Jwts.parser()
                    .verifyWith(userSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);

            if (!"USER".equals(role)) {
                return null;
            }

            return Long.parseLong(subject);
        } catch (Exception e) {
            return null;
        }
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setNickname("用户");
        user.setAvatarUrl("/default-avatar.png");
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String generateUserToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + userJwtExpiration))
                .signWith(userSecretKey)
                .compact();
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        return response;
    }
}
