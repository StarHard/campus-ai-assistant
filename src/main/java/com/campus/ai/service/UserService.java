package com.campus.ai.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.entity.User;

public interface UserService extends IService<User> {
    User login(LoginRequest request);
    User register(RegisterRequest request);
    User getByUsername(String username);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
