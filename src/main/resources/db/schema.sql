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

-- ============================================================
-- 聊天会话与消息表（组长A负责）
-- ============================================================

DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    `role`        VARCHAR(20)  NOT NULL COMMENT '角色(user/assistant)',
    `content`     TEXT         NOT NULL COMMENT '消息内容',
    `sources`     TEXT         DEFAULT NULL COMMENT 'RAG知识来源(JSON)',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
    `session_id`  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    `user_id`     VARCHAR(50)  NOT NULL COMMENT '用户ID',
    `title`       VARCHAR(100) NOT NULL DEFAULT '新会话' COMMENT '会话标题',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- ============================================================
-- 初始化教室数据（组员C：前端演示用）
-- ============================================================
INSERT INTO `campus_classroom` (`room_name`) VALUES
('A101'), ('A102'), ('A201'), ('A202'), ('A301'), ('A302'),
('B101'), ('B102'), ('B103'), ('B201'), ('B202'),
('S101'), ('S102'), ('S201'), ('S202'),
('T101'), ('T102'), ('T201'), ('T202'), ('T301');

-- ============================================================
-- 初始化课程数据（组员C：前端演示用，福建理工大学）
-- ============================================================
-- 计算机与信息科学学院 课程（2024-2025-1 学期）
INSERT INTO `campus_course` (`course_code`, `course_name`, `teacher_id`, `teacher_name`, `department`, `credit`, `course_hours`, `theory_hours`, `practice_hours`, `course_type`, `exam_type`, `semester`, `description`, `schedule_time`, `classroom_name`) VALUES
('CS001', '高等数学A(二)', NULL, '吴明远', '计算机与信息科学学院', 5.0, 80, 64, 16, 1, 1, '2024-2025-1', '函数极限连续、一元函数微积分学、多元函数微积分学、无穷级数、常微分方程', '周一1-2节,周三1-2节', 'A101'),
('CS002', '线性代数', NULL, '林志强', '计算机与信息科学学院', 3.0, 48, 48, 0, 1, 1, '2024-2025-1', '行列式、矩阵及其运算、向量组的线性相关性、线性方程组、特征值与特征向量、二次型', '周二3-4节,周四3-4节', 'A201'),
('CS003', '数据结构与算法', 'teacher001', '王建国', '计算机与信息科学学院', 4.0, 72, 52, 20, 1, 1, '2024-2025-1', '线性表、栈与队列、树与二叉树、图、查找与排序算法、复杂度分析', '周一3-4节,周五3-4节', 'S101'),
('CS004', '计算机组成原理', NULL, '陈伟民', '计算机与信息科学学院', 4.0, 64, 48, 16, 1, 1, '2024-2025-1', '计算机系统概论、运算方法与运算器、存储系统、指令系统、CPU结构与控制、输入输出系统', '周二1-2节,周四1-2节', 'S102'),
('CS005', '计算机网络', NULL, '黄晓峰', '计算机与信息科学学院', 3.5, 56, 44, 12, 1, 2, '2024-2025-1', '物理层、数据链路层、网络层、运输层、应用层、网络安全', '周五1-3节', 'S202'),
('CS006', '操作系统', 'teacher001', '王建国', '计算机与信息科学学院', 4.0, 72, 52, 20, 1, 1, '2024-2025-1', '进程管理、内存管理、文件系统、设备管理、死锁', '周三3-4节,周五1-2节', 'S101'),
('CS007', '大学英语(四)', NULL, '张雅琴', '计算机与信息科学学院', 2.0, 32, 32, 0, 1, 1, '2024-2025-1', '学术英语阅读与写作、英语演讲与辩论、科技英语翻译', '周二5-6节', 'B101'),
('CS008', '马克思主义基本原理', NULL, '刘建军', '计算机与信息科学学院', 3.0, 48, 48, 0, 1, 3, '2024-2025-1', '世界的物质性及发展规律、实践与认识、人类社会及其发展规律、资本主义的本质及规律', '周四5-7节', 'B103'),
('CS009', '数据库原理', NULL, '赵明辉', '计算机与信息科学学院', 3.0, 48, 32, 16, 2, 4, '2024-2025-1', '关系模型、SQL语言、数据库设计、事务与并发控制、数据库安全', '周三5-7节', 'S202'),
('CS010', 'Python程序设计', NULL, '周雪梅', '计算机与信息科学学院', 2.5, 48, 16, 32, 3, 4, '2024-2025-1', 'Python基础语法、函数与模块、面向对象编程、文件操作、爬虫基础、数据分析入门', '周五5-7节', 'S201');

-- 土木工程学院 课程（2024-2025-1 学期）
INSERT INTO `campus_course` (`course_code`, `course_name`, `teacher_id`, `teacher_name`, `department`, `credit`, `course_hours`, `theory_hours`, `practice_hours`, `course_type`, `exam_type`, `semester`, `description`, `schedule_time`, `classroom_name`) VALUES
('CE001', '高等数学A(二)', NULL, '吴明远', '土木工程学院', 5.0, 80, 64, 16, 1, 1, '2024-2025-1', '函数极限连续、一元函数微积分学、多元函数微积分学、无穷级数、常微分方程', '周一1-2节,周三1-2节', 'A101'),
('CE002', '线性代数', NULL, '林志强', '土木工程学院', 3.0, 48, 48, 0, 1, 1, '2024-2025-1', '行列式、矩阵及其运算、向量组的线性相关性、线性方程组、特征值与特征向量、二次型', '周二1-2节,周四1-2节', 'A201'),
('CE003', '大学物理B', NULL, '郑国栋', '土木工程学院', 4.0, 64, 48, 16, 1, 1, '2024-2025-1', '质点力学、刚体力学、热学、电磁学、波动光学、近代物理基础', '周二3-4节,周四3-4节', 'B102'),
('CE004', '理论力学', NULL, '许文斌', '土木工程学院', 4.0, 64, 64, 0, 1, 1, '2024-2025-1', '静力学、运动学、动力学三大体系，力系简化、平衡方程、点的合成运动、动静法', '周三3-4节,周五1-2节', 'T102'),
('CE005', '材料力学', NULL, '吴建国', '土木工程学院', 3.5, 56, 48, 8, 1, 1, '2024-2025-1', '拉伸压缩与剪切、扭转、弯曲内力与应力、应力状态与强度理论、组合变形、压杆稳定', '周一3-4节,周三5-6节', 'T102'),
('CE006', '工程制图', NULL, '孙丽华', '土木工程学院', 3.0, 56, 24, 32, 1, 4, '2024-2025-1', '制图基本知识、投影法、组合体、机件表达方法、建筑施工图、结构施工图', '周四5-8节', 'T201'),
('CE007', '土木工程材料', NULL, '何建华', '土木工程学院', 3.0, 48, 36, 12, 1, 1, '2024-2025-1', '气硬性胶凝材料、水泥、混凝土、砂浆、钢材、防水材料、保温材料', '周二5-7节', 'T201'),
('CE008', '大学英语(四)', NULL, '张雅琴', '土木工程学院', 2.0, 32, 32, 0, 1, 1, '2024-2025-1', '学术英语阅读与写作、英语演讲与辩论、科技英语翻译', '周五3-4节', 'B101'),
('CE009', '工程测量', NULL, '马德龙', '土木工程学院', 3.0, 48, 24, 24, 2, 4, '2024-2025-1', '水准测量、角度测量、距离测量、控制测量、地形图测绘、施工测量', '周三5-7节', 'T301'),
('CE010', '结构力学', NULL, '陈永强', '土木工程学院', 4.0, 64, 64, 0, 1, 1, '2024-2025-1', '几何组成分析、静定结构内力计算、虚功原理与结构位移计算、力法、位移法、力矩分配法', '周一5-7节,周五1-2节', 'T101');

-- ============================================================
-- 初始化排课数据（用于空教室查询算法）
-- ============================================================
INSERT INTO `campus_schedule` (`course_id`, `classroom_id`, `weekday`, `start_section`, `end_section`, `week_range`, `semester`) VALUES
-- 高数A(二) CS001 - A101
(1, 1, 1, 1, 2, '1-16', '2024-2025-1'),
(1, 1, 3, 1, 2, '1-16', '2024-2025-1'),
-- 线性代数 CS002 - A201
(2, 3, 2, 3, 4, '1-16', '2024-2025-1'),
(2, 3, 4, 3, 4, '1-16', '2024-2025-1'),
-- 数据结构 CS003 - S101
(3, 12, 1, 3, 4, '1-16', '2024-2025-1'),
(3, 12, 5, 3, 4, '1-16', '2024-2025-1'),
-- 计算机组成原理 CS004 - S102
(4, 13, 2, 1, 2, '1-16', '2024-2025-1'),
(4, 13, 4, 1, 2, '1-16', '2024-2025-1'),
-- 计算机网络 CS005 - S202
(5, 15, 5, 1, 3, '1-16', '2024-2025-1'),
-- 操作系统 CS006 - S101
(6, 12, 3, 3, 4, '1-16', '2024-2025-1'),
(6, 12, 5, 1, 2, '1-16', '2024-2025-1'),
-- 大学英语(四) CS007 - B101
(7, 7, 2, 5, 6, '1-16', '2024-2025-1'),
-- 马克思主义原理 CS008 - B103
(8, 9, 4, 5, 7, '1-16', '2024-2025-1'),
-- 数据库原理 CS009 - S202
(9, 15, 3, 5, 7, '1-16', '2024-2025-1'),
-- Python程序设计 CS010 - S201
(10, 14, 5, 5, 7, '1-16', '2024-2025-1'),
-- 高数A(二) CE001 - A101
(11, 1, 1, 1, 2, '1-16', '2024-2025-1'),
(11, 1, 3, 1, 2, '1-16', '2024-2025-1'),
-- 线性代数 CE002 - A201
(12, 3, 2, 1, 2, '1-16', '2024-2025-1'),
(12, 3, 4, 1, 2, '1-16', '2024-2025-1'),
-- 大学物理B CE003 - B102
(13, 8, 2, 3, 4, '1-16', '2024-2025-1'),
(13, 8, 4, 3, 4, '1-16', '2024-2025-1'),
-- 理论力学 CE004 - T102
(14, 17, 3, 3, 4, '1-16', '2024-2025-1'),
(14, 17, 5, 1, 2, '1-16', '2024-2025-1'),
-- 材料力学 CE005 - T102
(15, 17, 1, 3, 4, '1-16', '2024-2025-1'),
(15, 17, 3, 5, 6, '1-16', '2024-2025-1'),
-- 工程制图 CE006 - T201
(16, 18, 4, 5, 8, '1-16', '2024-2025-1'),
-- 土木工程材料 CE007 - T201
(17, 18, 2, 5, 7, '1-16', '2024-2025-1'),
-- 大学英语(四) CE008 - B101
(18, 7, 5, 3, 4, '1-16', '2024-2025-1'),
-- 工程测量 CE009 - T301
(19, 20, 3, 5, 7, '1-16', '2024-2025-1'),
-- 结构力学 CE010 - T101
(20, 16, 1, 5, 7, '1-16', '2024-2025-1'),
(20, 16, 5, 1, 2, '1-16', '2024-2025-1');

-- ============================================================
-- 初始化演示用户（组员C：前端演示用，密码统一123456）
-- ============================================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `salt`, `real_name`, `gender`, `phone`, `email`, `user_type`, `student_no`, `teacher_no`, `department`, `major`, `grade`, `status`) VALUES
('stu001', 'zhangsan', '653235859d90210438a8c5d9aaa33f89', 'comp2024001', '张明', 1, '13859100001', 'zhangsan@fjut.edu.cn', 1, '3204001', NULL, '计算机与信息科学学院', '计算机科学与技术', '2024', 1),
('stu002', 'lisi', '051f872d209fe7450833d91ce2506dfe', 'civi2024002', '李华', 1, '13859100002', 'lisi@fjut.edu.cn', 1, '3205002', NULL, '土木工程学院', '土木工程', '2024', 1),
('teacher001', 'wanglaoshi', '7f77572cc8614687962a1482db574009', 'teac2024003', '王建国', 1, '13859100003', 'wangjg@fjut.edu.cn', 2, NULL, '012024003', '计算机与信息科学学院', '计算机科学与技术', NULL, 1);

-- 演示用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
('stu001', 3),
('stu002', 3),
('teacher001', 2);
