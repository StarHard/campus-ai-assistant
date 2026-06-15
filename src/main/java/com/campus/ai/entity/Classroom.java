package com.campus.ai.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("campus_classroom")
public class Classroom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String buildingName;
    private String roomNumber;
    private String roomName;
    private Integer capacity;
    private Integer roomType;
    private Integer hasProjector;
    private Integer hasAirCond;
    private Integer floor;
    private Integer status;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
