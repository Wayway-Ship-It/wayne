# 数据库表结构

## 表 1: part_category（配件分类表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 分类ID |
| 2 | name | VARCHAR(50) | 否 | 是 | | 分类名称 |
| 3 | description | TEXT | 否 | 否 | | 分类描述 |
| 4 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 5 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 6 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |

## 表 2: part_info（配件信息表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 配件ID |
| 2 | part_code | VARCHAR(50) | 否 | 是 | | 配件编码 |
| 3 | part_name | VARCHAR(100) | 否 | 是 | | 配件名称 |
| 4 | category | VARCHAR(50) | 否 | 否 | | 分类 |
| 5 | brand | VARCHAR(50) | 否 | 否 | | 品牌 |
| 6 | model | VARCHAR(100) | 否 | 否 | | 型号 |
| 7 | specification | VARCHAR(200) | 否 | 否 | | 规格 |
| 8 | unit | VARCHAR(20) | 否 | 否 | | 单位 |
| 9 | price | DECIMAL(10,2) | 否 | 否 | | 价格 |
| 10 | manufacturer | VARCHAR(100) | 否 | 否 | | 制造商 |
| 11 | supplier | VARCHAR(100) | 否 | 否 | | 供应商 |
| 12 | description | TEXT | 否 | 否 | | 描述 |
| 13 | status | TINYINT | 否 | 否 | 1 | 状态 |
| 14 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 15 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 16 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |

## 表 3: stock_info（库存信息表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 库存ID |
| 2 | part_id | BIGINT | 否 | 是 | | 配件ID |
| 3 | part_code | VARCHAR(50) | 否 | 是 | | 配件编码 |
| 4 | part_name | VARCHAR(100) | 否 | 是 | | 配件名称 |
| 5 | quantity | INT | 否 | 否 | 0 | 数量 |
| 6 | safe_stock | INT | 否 | 否 | 0 | 安全库存 |
| 7 | warehouse_location | VARCHAR(100) | 否 | 否 | | 仓库位置 |
| 8 | batch_no | VARCHAR(50) | 否 | 否 | | 批次号 |
| 9 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 10 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 11 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |

## 表 4: inbound_order（入库订单表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 入库单ID |
| 2 | order_no | VARCHAR(50) | 否 | 是 | | 订单号 |
| 3 | part_id | BIGINT | 否 | 是 | | 配件ID |
| 4 | part_code | VARCHAR(50) | 否 | 是 | | 配件编码 |
| 5 | part_name | VARCHAR(100) | 否 | 是 | | 配件名称 |
| 6 | type | VARCHAR(20) | 否 | 否 | CGRK | 类型 |
| 7 | quantity | INT | 否 | 是 | | 数量 |
| 8 | unit_price | DECIMAL(10,2) | 否 | 否 | | 单价 |
| 9 | total_price | DECIMAL(10,2) | 否 | 否 | | 总价 |
| 10 | supplier | VARCHAR(100) | 否 | 否 | | 供应商名称 |
| 11 | batch_no | VARCHAR(50) | 否 | 否 | | 批次号 |
| 12 | warehouse_location | VARCHAR(100) | 否 | 否 | | 仓库位置 |
| 13 | operator | VARCHAR(50) | 否 | 否 | | 操作人 |
| 14 | remark | TEXT | 否 | 否 | | 备注 |
| 15 | status | TINYINT | 否 | 否 | 1 | 状态 |
| 16 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 17 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 18 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |
| 19 | supplier_id | BIGINT | 否 | 否 | | 供应商ID |

## 表 5: outbound_order（出库订单表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 出库单ID |
| 2 | order_no | VARCHAR(50) | 否 | 是 | | 订单号 |
| 3 | part_id | BIGINT | 否 | 是 | | 配件ID |
| 4 | part_code | VARCHAR(50) | 否 | 是 | | 配件编码 |
| 5 | part_name | VARCHAR(100) | 否 | 是 | | 配件名称 |
| 6 | type | VARCHAR(20) | 否 | 否 | XSCK | 类型 |
| 7 | quantity | INT | 否 | 是 | | 数量 |
| 8 | unit_price | DECIMAL(10,2) | 否 | 否 | | 单价 |
| 9 | total_price | DECIMAL(10,2) | 否 | 否 | | 总价 |
| 10 | receiver | VARCHAR(100) | 否 | 否 | | 接收人 |
| 11 | receiver_phone | VARCHAR(20) | 否 | 否 | | 接收人电话 |
| 12 | receiver_address | VARCHAR(255) | 否 | 否 | | 接收人地址 |
| 13 | warehouse_location | VARCHAR(100) | 否 | 否 | | 仓库位置 |
| 14 | applicant | VARCHAR(50) | 否 | 否 | | 申请人 |
| 15 | approver | VARCHAR(50) | 否 | 否 | | 审批人 |
| 16 | approval_remark | TEXT | 否 | 否 | | 审批备注 |
| 17 | approval_time | DATETIME | 否 | 否 | | 审批时间 |
| 18 | operator | VARCHAR(50) | 否 | 否 | | 操作人 |
| 19 | remark | TEXT | 否 | 否 | | 备注 |
| 20 | status | TINYINT | 否 | 否 | 0 | 状态 |
| 21 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 22 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 23 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |
| 24 | customer_id | BIGINT | 否 | 否 | | 客户ID |

## 表 6: stock_check（库存盘点表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 盘点单ID |
| 2 | check_no | VARCHAR(50) | 否 | 是 | | 盘点单号 |
| 3 | part_id | BIGINT | 否 | 否 | | 配件ID |
| 4 | part_code | VARCHAR(50) | 否 | 否 | | 配件编码 |
| 5 | part_name | VARCHAR(100) | 否 | 是 | | 配件名称 |
| 6 | system_quantity | INT | 否 | 否 | | 系统库存 |
| 7 | actual_quantity | INT | 否 | 否 | | 实际库存 |
| 8 | difference | INT | 否 | 否 | | 差异 |
| 9 | check_location | VARCHAR(100) | 否 | 否 | | 盘点位置 |
| 10 | checker | VARCHAR(50) | 否 | 否 | | 盘点人 |
| 11 | check_type | VARCHAR(20) | 否 | 否 | regular | 盘点类型 |
| 12 | period | VARCHAR(20) | 否 | 否 | | 盘点周期 |
| 13 | category | VARCHAR(50) | 否 | 否 | | 分类 |
| 14 | remark | TEXT | 否 | 否 | | 备注 |
| 15 | status | TINYINT | 否 | 否 | 0 | 状态 |
| 16 | confirm_status | TINYINT | 否 | 否 | 0 | 确认状态 |
| 17 | confirm_user | VARCHAR(50) | 否 | 否 | | 确认人 |
| 18 | confirm_time | DATETIME | 否 | 否 | | 确认时间 |
| 19 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 20 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 21 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |
| 22 | parts_summary | TEXT | 否 | 否 | | 配件汇总信息 |

## 表 7: sys_user（系统用户表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 用户ID |
| 2 | username | VARCHAR(50) | 否 | 是 | | 用户名 |
| 3 | password | VARCHAR(255) | 否 | 是 | | 密码 |
| 4 | real_name | VARCHAR(50) | 否 | 否 | | 真实姓名 |
| 5 | phone | VARCHAR(20) | 否 | 否 | | 电话 |
| 6 | email | VARCHAR(100) | 否 | 否 | | 邮箱 |
| 7 | role | VARCHAR(20) | 否 | 是 | USER | 角色 |
| 8 | status | TINYINT | 否 | 否 | 1 | 状态 |
| 9 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 10 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 11 | deleted | TINYINT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |

## 表 8: supplier（供应商表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 供应商ID |
| 2 | name | VARCHAR(100) | 否 | 是 | | 供应商名称 |
| 3 | address | VARCHAR(255) | 否 | 否 | | 地址 |
| 4 | phone | VARCHAR(20) | 否 | 是 | | 联系电话 |
| 5 | contact_person | VARCHAR(50) | 否 | 否 | | 联系人 |
| 6 | email | VARCHAR(100) | 否 | 否 | | 邮箱 |
| 7 | description | TEXT | 否 | 否 | | 描述 |
| 8 | status | INT | 否 | 否 | 1 | 状态：1-启用，0-禁用 |
| 9 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 10 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 11 | deleted | INT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |

## 表 9: customer（客户表）

| 序号 | 字段名 | 数据类型 | 主键 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | id | BIGINT | 是 | 是 | 自动递增 | 客户ID |
| 2 | name | VARCHAR(100) | 否 | 是 | | 客户名称 |
| 3 | address | VARCHAR(255) | 否 | 否 | | 地址 |
| 4 | phone | VARCHAR(20) | 否 | 是 | | 联系电话 |
| 5 | contact_person | VARCHAR(50) | 否 | 否 | | 联系人 |
| 6 | email | VARCHAR(100) | 否 | 否 | | 邮箱 |
| 7 | description | TEXT | 否 | 否 | | 描述 |
| 8 | status | INT | 否 | 否 | 1 | 状态：1-启用，0-禁用 |
| 9 | create_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 创建时间 |
| 10 | update_time | DATETIME | 否 | 否 | CURRENT_TIMESTAMP | 更新时间 |
| 11 | deleted | INT | 否 | 否 | 0 | 逻辑删除：0-未删除，1-已删除 |
