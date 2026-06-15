package com.campus.ai.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 标注在Controller方法上，限制只有指定角色的用户才能访问
 *
 * 使用示例:
 * @RequireRole("ADMIN")              - 仅管理员可访问
 * @RequireRole({"ADMIN","TEACHER"})  - 管理员或教师可访问
 * @RequireRole                       - 仅需登录即可（不限定角色）
 * 不标注                            - 公开接口，无需登录
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /** 允许访问的角色编码列表，空数组表示仅需登录 */
    String[] value() default {};
}
