package com.jack.login.controller;

import com.jack.login.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 管理接口：整个 /api/admin/** 在 SecurityConfig 中配置了 hasRole("ADMIN")
@Tag(name = "管理接口", description = "仅 ADMIN 角色可访问")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Operation(summary = "查看用户列表", description = "仅管理员可访问，普通用户返回 403")
    @GetMapping("/users")
    public Result<List<String>> users() {
        return Result.ok(List.of("admin(ADMIN)", "jack(USER)"));
    }
}
