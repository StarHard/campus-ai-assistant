package com.campus.ai.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.UserMapper;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.entity.User;
import com.campus.ai.service.UserService;
import com.campus.ai.util.Md5Util;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(LoginRequest request) {
        User user = getByUsername(request.getUsername());
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getStatus() == 0) throw new RuntimeException("账号已被禁用");
        if (!Md5Util.verify(request.getPassword(), user.getSalt(), user.getPassword())) throw new RuntimeException("密码错误");
        user.setLastLogin(LocalDateTime.now());
        updateById(user);
        user.setPassword(null);
        user.setSalt(null);
        return user;
    }

    @Override
    public User register(RegisterRequest request) {
        if (getByUsername(request.getUsername()) != null) throw new RuntimeException("该账号已注册");
        String salt = Md5Util.generateSalt();
        String encryptedPwd = Md5Util.encrypt(request.getPassword(), salt);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encryptedPwd);
        user.setSalt(salt);
        user.setRealName(request.getRealName());
        user.setUserType(request.getUserType() != null ? request.getUserType() : 2);
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setMajor(request.getMajor());
        user.setGrade(request.getGrade());
        user.setStatus(1);
        if (user.getUserType() == 1) user.setTeacherNo(request.getUsername());
        else user.setStudentNo(request.getUsername());
        save(user);
        user.setPassword(null);
        user.setSalt(null);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return getOne(wrapper);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!Md5Util.verify(oldPassword, user.getSalt(), user.getPassword())) throw new RuntimeException("原密码错误");
        String newSalt = Md5Util.generateSalt();
        user.setPassword(Md5Util.encrypt(newPassword, newSalt));
        user.setSalt(newSalt);
        updateById(user);
    }
}
