package com.autoparts.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("outbound_order")
public class Outbound {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long partId;

    private String partCode;

    private String partName;

    private Long customerId;

    private String type;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private String receiver;

    private String receiverPhone;

    private String receiverAddress;

    private String warehouseLocation;

    private String applicant;

    private String approver;

    private String approvalRemark;

    private LocalDateTime approvalTime;

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
