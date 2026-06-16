-- ============================================================
-- 校园智能服务小助手 - 数据库初始化脚本（组员B：邱春凯负责）
-- ============================================================

CREATE DATABASE IF NOT EXISTS campus_assistant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE campus_assistant;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(30)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(30)  NOT NULL COMMENT '角色编码',
    `description` VARCHAR(100) DEFAULT NULL COMMENT '角色描述',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态(1:启用 0:禁用)',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES
('超级管理员', 'ADMIN', '系统超级管理员'),
('教师',      'TEACHER', '教师用户'),
('学生',      'STUDENT', '学生用户');

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`            VARCHAR(50)  NOT NULL COMMENT '用户ID(学生00开头/教师01开头/admin)',
    `username`      VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`      VARCHAR(100) NOT NULL COMMENT '密码(MD5加盐)',
    `salt`          VARCHAR(32)  NOT NULL COMMENT '盐值',
    `real_name`     VARCHAR(30)  NOT NULL COMMENT '真实姓名',
    `gender`        TINYINT      DEFAULT NULL COMMENT '性别(1:男 2:女)',
    `phone`         VARCHAR(20)  DEFAULT NULL,
    `email`         VARCHAR(50)  DEFAULT NULL,
    `avatar`        VARCHAR(255) DEFAULT NULL,
    `user_type`     TINYINT      NOT NULL DEFAULT 2 COMMENT '用户类型(1:教师 2:学生)',
    `student_no`    VARCHAR(20)  DEFAULT NULL,
    `teacher_no`    VARCHAR(20)  DEFAULT NULL,
    `department`    VARCHAR(50)  DEFAULT NULL,
    `major`         VARCHAR(50)  DEFAULT NULL,
    `grade`         VARCHAR(10)  DEFAULT NULL,
    `status`        TINYINT      NOT NULL DEFAULT 1,
    `last_login`    DATETIME     DEFAULT NULL,
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

DROP TABLE IF EXISTS `campus_classroom`;
CREATE TABLE `campus_classroom` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `room_name`   VARCHAR(50)  NOT NULL,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_name` (`room_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室信息表';

DROP TABLE IF EXISTS `campus_course`;
CREATE TABLE `campus_course` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `course_code`     VARCHAR(20)  NOT NULL,
    `course_name`     VARCHAR(100) NOT NULL,
    `teacher_id`      VARCHAR(50)  DEFAULT NULL,
    `teacher_name`    VARCHAR(30)  DEFAULT NULL,
    `department`      VARCHAR(50)  DEFAULT NULL,
    `credit`          DECIMAL(3,1) DEFAULT NULL,
    `course_hours`    INT          DEFAULT NULL,
    `theory_hours`    INT          DEFAULT NULL,
    `practice_hours`  INT          DEFAULT NULL,
    `course_type`     TINYINT      DEFAULT NULL,
    `exam_type`       TINYINT      DEFAULT NULL,
    `semester`        VARCHAR(20)  DEFAULT NULL,
    `description`     VARCHAR(500) DEFAULT NULL,
    `schedule_time`   VARCHAR(100) DEFAULT NULL COMMENT '上课时间',
    `classroom_name`  VARCHAR(50)  DEFAULT NULL COMMENT '教室名称',
    `status`          TINYINT      NOT NULL DEFAULT 1,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程信息表';

DROP TABLE IF EXISTS `campus_schedule`;
CREATE TABLE `campus_schedule` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT,
    `course_id`     BIGINT   NOT NULL,
    `classroom_id`  BIGINT   NOT NULL,
    `weekday`       TINYINT  NOT NULL,
    `start_section` TINYINT  NOT NULL,
    `end_section`   TINYINT  NOT NULL,
    `week_range`    VARCHAR(20) DEFAULT NULL,
    `semester`      VARCHAR(20) NOT NULL,
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课表';

DROP TABLE IF EXISTS `campus_activity`;
CREATE TABLE `campus_activity` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `title`           VARCHAR(100)  NOT NULL,
    `content`         TEXT          DEFAULT NULL,
    `activity_type`   TINYINT       NOT NULL DEFAULT 1,
    `publisher_id`    BIGINT        DEFAULT NULL,
    `publisher_name`  VARCHAR(30)   DEFAULT NULL,
    `location`        VARCHAR(100)  DEFAULT NULL,
    `start_time`      DATETIME      NOT NULL,
    `end_time`        DATETIME      DEFAULT NULL,
    `max_participants` INT         DEFAULT NULL,
    `current_count`   INT          NOT NULL DEFAULT 0,
    `status`          TINYINT       NOT NULL DEFAULT 0,
    `cover_image`     VARCHAR(255)  DEFAULT NULL,
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园活动表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

DROP TABLE IF EXISTS `campus_reservation`;
CREATE TABLE `campus_reservation` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`       BIGINT      NOT NULL,
    `user_name`     VARCHAR(30) DEFAULT NULL,
    `activity_id`   BIGINT      NOT NULL,
    `reserve_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`        TINYINT     NOT NULL DEFAULT 0,
    `remark`        VARCHAR(255) DEFAULT NULL,
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`     VARCHAR(50) NOT NULL COMMENT '用户ID',
    `username`    VARCHAR(50) DEFAULT NULL,
    `ip_address`  VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    `user_agent`  VARCHAR(255) DEFAULT NULL,
    `login_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '1:成功 0:失败',
    `message`     VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 初始化管理员账号 (admin / 123456)
INSERT INTO `sys_user` (`id`, `username`, `password`, `salt`, `real_name`, `user_type`, `department`, `status`) VALUES
('admin', 'admin', 'cdfd6fbdd51d731b9aac36c5cd836519', 'abcd1234', '系统管理员', 0, '信息中心', 1);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
('admin', 1);
