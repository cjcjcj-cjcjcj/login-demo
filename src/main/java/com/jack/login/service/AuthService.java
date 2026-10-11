package com.jack.login.service;

import com.jack.login.dto.AuthUser;

// 认证服务接口：面向接口编程，实现类见 service/impl/AuthServiceImpl
public interface AuthService {

    /** 获取 RSA 公钥（Base64），前端用它加密密码 */
    String getPublicKey();

    /**
     * 登录，成功返回 JWT token（内含用户名和角色），失败返回 null
     * @param rsaEncryptedPassword 前端用 RSA 公钥加密后的密码
     */
    String login(String username, String rsaEncryptedPassword);

    /** 校验 JWT，有效返回当前用户（含角色），无效/过期/已登出返回 null */
    AuthUser validate(String token);

    /** 登出：把 token 加入黑名单 */
    void logout(String token);
}
