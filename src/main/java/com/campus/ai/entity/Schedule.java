package com.campus.ai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("campus_schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long classroomId;
    private Integer weekday;
    private Integer startSection;
    private Integer endSection;
    private String weekRange;
    private String semester;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
