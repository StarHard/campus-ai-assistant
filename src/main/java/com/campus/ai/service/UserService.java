package com.campus.ai.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.LoginResponse;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    User login(LoginRequest request);
    LoginResponse loginWithToken(LoginRequest request);
    User register(RegisterRequest request);
    User getByUsername(String username);
    void changePassword(Long userId, String oldPassword, String newPassword);
    /** 验证Token是否有效，返回用户ID，无效返回null */
    Long validateToken(String token);
    /** 使Token失效（登出） */
    void invalidateToken(String token);
    /** 获取用户的角色编码列表 */
    List<String> getUserRoles(Long userId);
}
