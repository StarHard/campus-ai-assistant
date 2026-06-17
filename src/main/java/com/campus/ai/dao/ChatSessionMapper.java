package com.campus.ai.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
