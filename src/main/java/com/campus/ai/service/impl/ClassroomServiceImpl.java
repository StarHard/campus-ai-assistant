package com.campus.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ai.dao.ClassroomMapper;
import com.campus.ai.entity.Classroom;
import com.campus.ai.service.ClassroomService;
import org.springframework.stereotype.Service;

@Service
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {
}
