package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.ChatMessageMapper;
import com.campus.ai.dao.ChatSessionMapper;
import com.campus.ai.entity.ChatMessage;
import com.campus.ai.entity.ChatSession;
import com.campus.ai.service.ChatSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天会话持久化服务实现
 *
 * @author A组长
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionServiceImpl.class);

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public String createSession(String userId, String title) {
        ChatSession session = new ChatSession();
        session.setSessionId(java.util.UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新会话");
        save(session);
        log.info("创建会话: sessionId={}, userId={}", session.getSessionId(), userId);
        return session.getSessionId();
    }

    @Override
    public void updateSessionTitle(String sessionId, String title) {
        ChatSession session = getById(sessionId);
        if (session != null) {
            session.setTitle(title);
            updateById(session);
        }
    }

    @Override
    public List<ChatSession> listSessions(String userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
               .orderByDesc(ChatSession::getUpdateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(String sessionId) {
        // 先删除该会话的所有消息
        LambdaQueryWrapper<ChatMessage> msgWrapper = new LambdaQueryWrapper<>();
        msgWrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(msgWrapper);
        // 再删除会话记录
        removeById(sessionId);
        log.info("删除会话: sessionId={}", sessionId);
    }

    @Override
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) return;
        for (ChatMessage msg : messages) {
            chatMessageMapper.insert(msg);
        }
    }

    @Override
    public List<ChatMessage> getMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreateTime);
        return chatMessageMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearMessages(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(wrapper);
        log.info("清空会话消息: sessionId={}", sessionId);
    }
}
