package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ai.dao.LoginLogMapper;
import com.campus.ai.entity.LoginLog;
import com.campus.ai.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public void recordLogin(Long userId, String username, String ip, String userAgent, boolean success, String message) {
        LoginLog loginLog = new LoginLog();
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setIpAddress(ip);
        loginLog.setUserAgent(userAgent);
        loginLog.setStatus(success ? 1 : 0);
        loginLog.setMessage(message);
        loginLogMapper.insert(loginLog);
    }

    @Override
    public IPage<LoginLog> queryLogs(Long userId, Integer page, Integer pageSize) {
        Page<LoginLog> pageObj = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(LoginLog::getUserId, userId);
        }
        wrapper.orderByDesc(LoginLog::getLoginTime);
        return loginLogMapper.selectPage(pageObj, wrapper);
    }
}
