package com.campus.ai.interceptor;

import com.campus.ai.annotation.RequireRole;
import com.campus.ai.config.BusinessException;
import com.campus.ai.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * 基于角色的权限拦截器
 *
 * 检查流程:
 * 1. 方法是否标注了@RequireRole → 没有则放行（公开接口）
 * 2. 从请求头获取 X-Token → 没有则返回401
 * 3. 验证Token有效性，获取userId
 * 4. 查询用户的角色列表
 * 5. ADMIN角色直接放行，否则检查是否匹配所需角色
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "X-Token";
    public static final String USER_ID_ATTR = "currentUserId";
    public static final String USER_ROLES_ATTR = "currentUserRoles";

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 非Controller方法直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 检查方法级别和类级别的 @RequireRole 注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            // 没有标注@RequireRole，视为公开接口
            return true;
        }

        String[] requiredRoles = requireRole.value();

        // 从请求头获取token
        String token = request.getHeader(TOKEN_HEADER);
        if (token == null || token.isBlank()) {
            throw new BusinessException(401, "未登录，请在请求头添加 X-Token");
        }

        // 验证token并获取用户ID
        Long userId = userService.validateToken(token);
        if (userId == null) {
            throw new BusinessException(401, "令牌无效或已过期，请重新登录");
        }

        // 获取用户角色列表
        List<String> userRoles = userService.getUserRoles(userId);

        // 将用户信息存入request属性，供Controller使用
        request.setAttribute(USER_ID_ATTR, userId);
        request.setAttribute(USER_ROLES_ATTR, userRoles);

        // 权限校验：ADMIN拥有所有权限
        if (userRoles.contains("ADMIN")) {
            return true;
        }

        // 检查是否拥有任一所需角色（如果指定了具体角色）
        if (requiredRoles.length > 0) {
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
            throw new BusinessException(403, "权限不足，需要角色: " + String.join("/", requiredRoles));
        }

        // 标注了@RequireRole但没指定角色 → 只需登录即放行
        return true;
    }
}
