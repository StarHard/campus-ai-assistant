package com.campus.ai.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
