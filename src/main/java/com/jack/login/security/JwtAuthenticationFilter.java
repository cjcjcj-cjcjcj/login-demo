package com.jack.login.security;

import com.jack.login.dto.AuthUser;
import com.jack.login.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// JWT 认证过滤器：从 Authorization: Bearer <token> 解析出用户名和角色，写入 SecurityContext
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final AuthService authService;

    public JwtAuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            AuthUser user = authService.validate(token);
            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 角色从 JWT 的 role claim 中取出，拼成 Spring Security 约定的 ROLE_XXX 形式
                var authentication = new UsernamePasswordAuthenticationToken(
                        user.username(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 认证通过：{} {} 用户={} 角色={}",
                        request.getMethod(), request.getRequestURI(), user.username(), user.role());
            }
        }
        filterChain.doFilter(request, response);
    }
}
