package com.autoparts.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stock_check")
public class StockCheck {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private Long partId;

    private String partCode;

    private String partName;

    private String category;    // 配件分类
    
    private String period;      // 盘点周期：monthly-每月，quarterly-每季度，yearly-年末
    
    private String partsSummary; // 配件汇总信息（定期盘点时存储所有配件的汇总）

    private Integer systemQuantity;

    private Integer actualQuantity;

    private Integer difference;

    private String checkLocation;

    private String checker;

    private String remark;

    private Integer status;

    private Integer confirmStatus;

    private String confirmUser;

    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
