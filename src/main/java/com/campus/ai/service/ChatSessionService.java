package com.campus.ai.service;

import com.campus.ai.entity.ChatMessage;
import com.campus.ai.entity.ChatSession;
import java.util.List;
import java.util.Map;

/**
 * 聊天会话持久化服务
 * 负责会话和消息的数据库CRUD操作
 *
 * @author A组长
 */
public interface ChatSessionService {

    /**
     * 创建新会话
     * @param userId 用户ID
     * @param title 会话标题
     * @return 会话ID
     */
    String createSession(String userId, String title);

    /**
     * 更新会话标题
     */
    void updateSessionTitle(String sessionId, String title);

    /**
     * 获取用户的所有会话列表（按更新时间倒序）
     */
    List<ChatSession> listSessions(String userId);

    /**
     * 删除会话及其所有消息
     */
    void deleteSession(String sessionId);

    /**
     * 保存一条消息
     */
    void saveMessage(ChatMessage message);

    /**
     * 批量保存消息（用于流式结束后一次性存储）
     */
    void saveMessages(List<ChatMessage> messages);

    /**
     * 获取会话的历史消息列表
     * @param sessionId 会话ID
     * @return 消息列表（按时间正序）
     */
    List<ChatMessage> getMessages(String sessionId);

    /**
     * 清空会话的所有消息（保留会话记录）
     */
    void clearMessages(String sessionId);
}
