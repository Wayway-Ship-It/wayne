package com.autoparts.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inbound_order")
public class Inbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long partId;

    private String partCode;

    private String partName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private Long supplierId;

    private String supplier;

    private String batchNo;

    private String type;

    private String warehouseLocation;

    private String operator;

    private String remark;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
