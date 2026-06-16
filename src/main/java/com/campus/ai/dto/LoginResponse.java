package com.campus.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 * 包含Token和用户完整信息，供前端保存和使用
 */
public class LoginResponse {
    private String userId;
    private String username;
    private String realName;
    private Integer userType;
    private String userTypeName;   // 1=教师 2=学生
    private String department;
    private String avatar;
    private LocalDateTime loginTime;
    private String token;          // 访问令牌，后续请求放入X-Token头
    private List<String> roles;    // 用户角色列表
    private List<String> roleNames;// 角色中文名称列表

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }
    public String getUserTypeName() { return userTypeName; }
    public void setUserTypeName(String userTypeName) { this.userTypeName = userTypeName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getRoleNames() { return roleNames; }
    public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames; }
}
