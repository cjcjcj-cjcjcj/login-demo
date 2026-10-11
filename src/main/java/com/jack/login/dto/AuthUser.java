package com.jack.login.dto;

// 从 JWT 中解析出的当前登录用户信息
public record AuthUser(String username, String role) {
}
