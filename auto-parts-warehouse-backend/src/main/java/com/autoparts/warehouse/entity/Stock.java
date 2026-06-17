package com.autoparts.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stock_info")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partId;

    private String partCode;

    private String partName;

    private Integer quantity;

    private Integer safeStock;

    private String warehouseLocation;

    private String batchNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
