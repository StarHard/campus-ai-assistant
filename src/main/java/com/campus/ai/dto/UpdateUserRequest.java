package com.campus.ai.dto;

import java.io.Serializable;

public class UpdateUserRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String realName;
    private Integer gender;
    private String phone;
    private String email;
    private String department;
    private String major;
    private String grade;
    private String avatar;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
