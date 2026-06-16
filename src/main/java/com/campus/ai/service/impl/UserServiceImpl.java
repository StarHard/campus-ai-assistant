package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.RoleMapper;
import com.campus.ai.dao.UserMapper;
import com.campus.ai.dao.UserRoleMapper;
import com.campus.ai.dto.LoginRequest;
import com.campus.ai.dto.LoginResponse;
import com.campus.ai.dto.RegisterRequest;
import com.campus.ai.dto.UpdateUserRequest;
import com.campus.ai.entity.Role;
import com.campus.ai.entity.User;
import com.campus.ai.entity.UserRole;
import com.campus.ai.service.LoginLogService;
import com.campus.ai.service.UserService;
import com.campus.ai.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 内存Token存储：key=token, value=userId */
    private final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private LoginLogService loginLogService;

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
    public LoginResponse loginWithToken(LoginRequest request) {
        String username = request.getUsername();
        try {
            User user = login(request);
            // 记录登录成功日志
            loginLogService.recordLogin(user.getId(), username, null, null, true, "登录成功");
            // 生成Token并存储
            String token = UUID.randomUUID().toString().replace("-", "");
            tokenStore.put(token, user.getId());

            // 查询用户角色
            List<String> roles = getUserRoles(user.getId());
            List<String> roleNames = getRoleNames(roles);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setUserType(user.getUserType());
            String typeName;
            if (user.getUserType() == 1) typeName = "教师";
            else if (user.getUserType() == 2) typeName = "学生";
            else typeName = "管理员";
            response.setUserTypeName(typeName);
            response.setDepartment(user.getDepartment());
            response.setAvatar(user.getAvatar());
            response.setLoginTime(user.getLastLogin());
            response.setToken(token);
            response.setRoles(roles);
            response.setRoleNames(roleNames);
            return response;
        } catch (RuntimeException e) {
            // 记录登录失败日志
            String userId = null;
            User tempUser = getByUsername(username);
            if (tempUser != null) userId = tempUser.getId();
            loginLogService.recordLogin(userId, username, null, null, false, e.getMessage());
            throw e;
        }
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
        // userId 以00/01开头：直接用username作为主键ID
        user.setId(request.getUsername());
        if (user.getUserType() == 1) user.setTeacherNo(request.getUsername());
        else user.setStudentNo(request.getUsername());
        save(user);

        // 注册时自动分配默认角色
        assignDefaultRole(user.getId(), user.getUserType());

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
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!Md5Util.verify(oldPassword, user.getSalt(), user.getPassword())) throw new RuntimeException("原密码错误");
        String newSalt = Md5Util.generateSalt();
        user.setPassword(Md5Util.encrypt(newPassword, newSalt));
        user.setSalt(newSalt);
        updateById(user);
    }

    @Override
    public String validateToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    @Override
    public List<String> getUserRoles(String userId) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        List<UserRole> userRoles = userRoleMapper.selectList(wrapper);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询角色编码
        List<Long> roleIds = new ArrayList<>();
        for (UserRole ur : userRoles) {
            roleIds.add(ur.getRoleId());
        }
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(Role::getId, roleIds);
        List<Role> roles = roleMapper.selectList(roleWrapper);
        List<String> roleCodes = new ArrayList<>();
        for (Role r : roles) {
            roleCodes.add(r.getRoleCode());
        }
        return roleCodes;
    }

    /**
     * 根据用户类型分配默认角色：教师→TEACHER，学生→STUDENT
     */
    private void assignDefaultRole(String userId, Integer userType) {
        String roleCode = (userType != null && userType == 1) ? "TEACHER" : "STUDENT";
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode);
        Role role = roleMapper.selectOne(wrapper);
        if (role != null) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }
    }

    /**
     * 根据角色编码列表查询角色中文名称
     */
    private List<String> getRoleNames(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Role::getRoleCode, roleCodes);
        List<Role> roles = roleMapper.selectList(wrapper);
        List<String> names = new ArrayList<>();
        for (Role r : roles) {
            names.add(r.getRoleName());
        }
        return names;
    }

    @Override
    public void updateUser(String id, UpdateUserRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getMajor() != null) user.setMajor(request.getMajor());
        if (request.getGrade() != null) user.setGrade(request.getGrade());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        updateById(user);
    }

    @Override
    public void deactivateUser(String userId) {
        User user = getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setStatus(0);
        updateById(user);
    }
}
