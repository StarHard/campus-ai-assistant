package com.campus.ai.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String realName;
    private Integer userType = 2;
    private Integer gender;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") private String phone;
    private String email;
    private String department;
    private String major;
    private String grade;
    public String getUsername() { return username; } public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; } public void setPassword(String v) { this.password = v; }
    public String getRealName() { return realName; } public void setRealName(String v) { this.realName = v; }
    public Integer getUserType() { return userType; } public void setUserType(Integer v) { this.userType = v; }
    public Integer getGender() { return gender; } public void setGender(Integer v) { this.gender = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { this.phone = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { this.department = v; }
    public String getMajor() { return major; } public void setMajor(String v) { this.major = v; }
    public String getGrade() { return grade; } public void setGrade(String v) { this.grade = v; }
}
