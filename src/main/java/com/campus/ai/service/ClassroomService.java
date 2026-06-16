package com.campus.ai.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ai.entity.Classroom;
import java.util.List;

public interface ClassroomService extends IService<Classroom> {
    List<Classroom> findEmptyClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester);
}
