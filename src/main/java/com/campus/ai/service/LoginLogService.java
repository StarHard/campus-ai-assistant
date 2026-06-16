package com.campus.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.ai.entity.LoginLog;

public interface LoginLogService {

    void recordLogin(Long userId, String username, String ip, String userAgent, boolean success, String message);

    IPage<LoginLog> queryLogs(Long userId, Integer page, Integer pageSize);
}
