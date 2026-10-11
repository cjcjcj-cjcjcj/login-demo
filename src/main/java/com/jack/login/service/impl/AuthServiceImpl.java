package com.jack.login.service.impl;

import com.jack.login.dto.AuthUser;
import com.jack.login.service.AuthService;
import com.jack.login.util.CryptoUtil;
import com.jack.login.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// AuthService 的实现类
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    // 用户账号：AES 加密后的密码 + 角色
    private record UserAccount(String encryptedPassword, String role) {
    }

    @Value("${app.aes-secret}")
    private String aesSecret;

    private final JwtUtil jwtUtil;

    private final Map<String, UserAccount> userStore = new ConcurrentHashMap<>();
    // JWT 是无状态的，登出通过黑名单实现
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    private KeyPair rsaKeyPair;

    public AuthServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void init() throws Exception {
        userStore.put("admin", new UserAccount(CryptoUtil.aesEncrypt("123456", aesSecret), "ADMIN"));
        userStore.put("jack", new UserAccount(CryptoUtil.aesEncrypt("abcdef", aesSecret), "USER"));
        rsaKeyPair = CryptoUtil.generateRseKeyPair();
        log.info("用户数据初始化完成，共{}个用户", userStore.size());
    }

    @Override
    public String getPublicKey() {
        return CryptoUtil.rsaPublicKeyToBase64(rsaKeyPair.getPublic());
    }

    @Override
    public String login(String username, String rsaEncryptedPassword) {
        try {
            String plain = CryptoUtil.rseDecrypt(rsaEncryptedPassword, rsaKeyPair.getPrivate());
            UserAccount account = userStore.get(username);
            if (account != null && account.encryptedPassword().equals(CryptoUtil.aesEncrypt(plain, aesSecret))) {
                String token = jwtUtil.generateToken(username, account.role());
                log.info("用户{}登录成功，签发JWT，角色={}", username, account.role());
                return token;
            }
            log.warn("用户{}登录失败：用户名或密码错误", username);
            return null;
        } catch (Exception e) {
            log.error("登录处理异常", e);
            return null;
        }
    }

    @Override
    public AuthUser validate(String token) {
        if (token == null || tokenBlacklist.contains(token)) {
            return null;
        }
        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            return null;
        }
        return new AuthUser(claims.getSubject(), claims.get("role", String.class));
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.add(token);
        log.info("token已加入黑名单（登出）");
    }
}
