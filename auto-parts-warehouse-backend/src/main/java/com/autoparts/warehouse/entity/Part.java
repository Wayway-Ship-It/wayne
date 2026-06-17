package com.autoparts.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part_info")
public class Part {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String partCode;

    private String partName;

    private String category;

    private String brand;

    private String model;

    private String specification;

    private String unit;

    private BigDecimal price;

    private String manufacturer;

    private String supplier;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
