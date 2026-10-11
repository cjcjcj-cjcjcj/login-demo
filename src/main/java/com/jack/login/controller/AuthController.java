package com.jack.login.controller;

import com.jack.login.common.Result;
import com.jack.login.dto.LoginRequest;
import com.jack.login.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

//"我是接口类" @RestController = @Controller + @ResponseBody
@Tag(name = "认证接口", description = "登录、登出、获取公钥、用户信息")
@RestController
//"本类所有接口路径都以 /api 开头"
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "获取 RSA 公钥", description = "前端用该公钥加密登录密码")
    @GetMapping("/auth/publicKey")
    public Result<String> publicKey() {
        return Result.ok(authService.getPublicKey());
    }

    @Operation(summary = "登录", description = "密码需用 RSA 公钥加密后传输，成功返回 JWT")
    @PostMapping("/auth/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        if (token == null)
            return Result.fail(400, "用户名或密码错误");
        return Result.ok(Map.of("token", token));
    }

    @Operation(summary = "退出登录", description = "把当前 JWT 加入黑名单")
    @PostMapping("/auth/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String header) {
        // 能走到这里说明 Security 已认证通过，token 有效
        authService.logout(header.substring(7));
        return Result.ok(null);
    }

    @Operation(summary = "获取当前用户信息", description = "从 SecurityContext 中取登录用户名和角色")
    @GetMapping("/user/profile")
    public Result<Map<String, String>> profile(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("无角色");
        return Result.ok(Map.of("username", authentication.getName(), "role", role));
    }
}
