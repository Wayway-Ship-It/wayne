-- 创建数据库
CREATE DATABASE IF NOT EXISTS auto_parts_warehouse DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE auto_parts_warehouse;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通操作员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 配件分类表
DROP TABLE IF EXISTS part_category;
CREATE TABLE part_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件分类表';

-- 配件信息表
DROP TABLE IF EXISTS part_info;
CREATE TABLE part_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    part_code VARCHAR(50) NOT NULL UNIQUE COMMENT '配件编码',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    category VARCHAR(50) COMMENT '分类',
    brand VARCHAR(50) COMMENT '品牌',
    model VARCHAR(100) COMMENT '型号',
    specification VARCHAR(200) COMMENT '规格',
    unit VARCHAR(20) COMMENT '单位',
    price DECIMAL(10,2) COMMENT '价格',
    manufacturer VARCHAR(100) COMMENT '制造商',
    supplier VARCHAR(100) COMMENT '供应商',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_part_code (part_code),
    INDEX idx_category (category),
    INDEX idx_part_name (part_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件信息表';

-- 库存信息表
DROP TABLE IF EXISTS stock_info;
CREATE TABLE stock_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    part_code VARCHAR(50) NOT NULL COMMENT '配件编码',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    quantity INT DEFAULT 0 COMMENT '库存数量',
    safe_stock INT DEFAULT 0 COMMENT '安全库存',
    warehouse_location VARCHAR(100) COMMENT '库位',
    batch_no VARCHAR(50) COMMENT '批次号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_part_id (part_id),
    INDEX idx_part_code (part_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存信息表';

-- 入库单表
DROP TABLE IF EXISTS inbound_order;
CREATE TABLE inbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '入库单号',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    part_code VARCHAR(50) NOT NULL COMMENT '配件编码',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    type VARCHAR(20) DEFAULT 'CGRK' COMMENT '入库类型：CGRK-采购入库，THRK-退货入库',
    quantity INT NOT NULL COMMENT '入库数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    total_price DECIMAL(10,2) COMMENT '总价',
    supplier VARCHAR(100) COMMENT '供应商',
    batch_no VARCHAR(50) COMMENT '批次号',
    warehouse_location VARCHAR(100) COMMENT '库位',
    operator VARCHAR(50) COMMENT '操作人',
    remark TEXT COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_order_no (order_no),
    INDEX idx_part_id (part_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单表';

-- 出库单表
DROP TABLE IF EXISTS outbound_order;
CREATE TABLE outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '出库单号',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    part_code VARCHAR(50) NOT NULL COMMENT '配件编码',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    type VARCHAR(20) DEFAULT 'XSCK' COMMENT '出库类型：XSCK-销售出库，DBCK-调拨出库，BFCK-报废出库',
    quantity INT NOT NULL COMMENT '出库数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    total_price DECIMAL(10,2) COMMENT '总价',
    receiver VARCHAR(100) COMMENT '收货人',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    receiver_address VARCHAR(255) COMMENT '收货地址',
    warehouse_location VARCHAR(100) COMMENT '库位',
    applicant VARCHAR(50) COMMENT '申请人',
    approver VARCHAR(50) COMMENT '审批人',
    approval_remark TEXT COMMENT '审批备注',
    approval_time DATETIME COMMENT '审批时间',
    operator VARCHAR(50) COMMENT '操作人',
    remark TEXT COMMENT '备注',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审批，1-已审批，2-已完成，3-已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_order_no (order_no),
    INDEX idx_part_id (part_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单表';

-- 库存盘点表
DROP TABLE IF EXISTS stock_check;
CREATE TABLE stock_check (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    check_no VARCHAR(50) NOT NULL UNIQUE COMMENT '盘点单号',
    part_id BIGINT NOT NULL COMMENT '配件ID',
    part_code VARCHAR(50) NOT NULL COMMENT '配件编码',
    part_name VARCHAR(100) NOT NULL COMMENT '配件名称',
    system_quantity INT COMMENT '系统数量',
    actual_quantity INT COMMENT '实际数量',
    difference INT COMMENT '差异',
    check_location VARCHAR(100) COMMENT '盘点位置',
    checker VARCHAR(50) COMMENT '盘点人',
    check_type VARCHAR(20) DEFAULT 'regular' COMMENT '盘点类型：regular-定期盘点，cycle-循环盘点',
    period VARCHAR(20) COMMENT '盘点周期：monthly-每月，quarterly-每季度，yearly-年末',
    category VARCHAR(50) COMMENT '配件分类（循环盘点时使用）',
    remark TEXT COMMENT '备注',
    status TINYINT DEFAULT 0 COMMENT '状态：0-盘点中，1-已完成',
    confirm_status TINYINT DEFAULT 0 COMMENT '确认状态：0-待确认，1-已确认',
    confirm_user VARCHAR(50) COMMENT '确认人',
    confirm_time DATETIME COMMENT '确认时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_check_no (check_no),
    INDEX idx_part_id (part_id),
    INDEX idx_create_time (create_time),
    INDEX idx_check_type (check_type),
    INDEX idx_confirm_status (confirm_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点表';

-- 初始化数据
-- 插入管理员用户（密码：admin123，已加密）
INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('admin', '$2a$10$lLlEXozVSm4XQR2076wJZu8iF62xxwjN4bovLfsEk022eoNNTJpgK', '系统管理员', '13800138000', 'admin@autoparts.com', 'ADMIN', 1),
('user', '$2a$10$4EG/d34lOEeCTfRYnGEo4OlmJBPdndNx.O2YCZwaLmFh9zYd.I33O', '普通用户', '13800138001', 'user@autoparts.com', 'USER', 1);

-- 插入配件分类数据
INSERT INTO part_category (name, description) VALUES
('发动机配件', '发动机相关配件，包括缸体、缸盖、活塞、曲轴、连杆、气门、凸轮轴、油底壳等'),
('传动系配件', '动力传输相关配件，如离合器、变速器、传动轴等'),
('制动系配件', '行车安全核心配件，如制动总泵、刹车鼓、刹车片等'),
('转向系配件', '操控方向相关配件，如主销、转向节、转向器等'),
('行驶系配件', '车身支撑与行走部件，如后桥、悬架系统、轮胎等'),
('电气仪表及电子配件', '汽车智能化运行相关配件，如传感器、ECU、开关、灯具、喇叭、继电器等'),
('车身及附件', '汽车外观与驾乘保障部件，如车门、车窗、车灯、内外饰件、安全气囊、安全带等');

-- 插入库存数据
INSERT INTO stock_info (part_id, part_code, part_name, quantity, safe_stock, warehouse_location, batch_no) VALUES
-- 发动机配件
(1, 'ENG-001', '缸体', 15, 3, 'A-01-01', 'ENG-20260301'),
(2, 'ENG-002', '缸盖', 20, 5, 'A-01-02', 'ENG-20260301'),
(3, 'ENG-003', '活塞', 50, 10, 'A-01-03', 'ENG-20260301'),
(4, 'ENG-004', '曲轴', 12, 3, 'A-01-04', 'ENG-20260301'),
(5, 'ENG-005', '连杆', 30, 8, 'A-01-05', 'ENG-20260301'),
(6, 'ENG-006', '进气门', 80, 20, 'A-01-06', 'ENG-20260301'),
(7, 'ENG-007', '凸轮轴', 18, 5, 'A-01-07', 'ENG-20260301'),
(8, 'ENG-008', '油底壳', 25, 8, 'A-01-08', 'ENG-20260301'),
-- 传动系配件
(9, 'TRA-001', '离合器压盘', 25, 8, 'A-02-01', 'TRA-20260301'),
(10, 'TRA-002', '离合器片', 40, 12, 'A-02-02', 'TRA-20260301'),
(11, 'TRA-003', '变速器总成', 8, 2, 'A-02-03', 'TRA-20260301'),
(12, 'TRA-004', '传动轴', 15, 4, 'A-02-04', 'TRA-20260301'),
(13, 'TRA-005', '差速器', 10, 3, 'A-02-05', 'TRA-20260301'),
-- 制动系配件
(14, 'BRA-001', '制动总泵', 20, 6, 'A-03-01', 'BRA-20260301'),
(15, 'BRA-002', '前刹车片', 60, 15, 'A-03-02', 'BRA-20260301'),
(16, 'BRA-003', '后刹车片', 50, 12, 'A-03-03', 'BRA-20260301'),
(17, 'BRA-004', '刹车鼓', 30, 8, 'A-03-04', 'BRA-20260301'),
(18, 'BRA-005', '制动软管', 100, 25, 'A-03-05', 'BRA-20260301'),
-- 转向系配件
(19, 'STE-001', '转向器', 15, 4, 'A-04-01', 'STE-20260301'),
(20, 'STE-002', '转向节', 25, 8, 'A-04-02', 'STE-20260301'),
(21, 'STE-003', '主销', 60, 15, 'A-04-03', 'STE-20260301'),
(22, 'STE-004', '横拉杆', 40, 10, 'A-04-04', 'STE-20260301'),
-- 行驶系配件
(23, 'RUN-001', '后桥总成', 8, 2, 'A-05-01', 'RUN-20260301'),
(24, 'RUN-002', '前悬架总成', 12, 3, 'A-05-02', 'RUN-20260301'),
(25, 'RUN-003', '减震器', 35, 10, 'A-05-03', 'RUN-20260301'),
(26, 'RUN-004', '轮胎', 50, 15, 'A-05-04', 'RUN-20260301'),
(27, 'RUN-005', '轮毂', 30, 8, 'A-05-05', 'RUN-20260301'),
-- 电气仪表及电子配件
(28, 'ELE-001', '水温传感器', 80, 20, 'A-06-01', 'ELE-20260301'),
(29, 'ELE-002', '氧传感器', 40, 10, 'A-06-02', 'ELE-20260301'),
(30, 'ELE-003', 'ECU 控制单元', 10, 3, 'A-06-03', 'ELE-20260301'),
(31, 'ELE-004', '点火开关', 50, 15, 'A-06-04', 'ELE-20260301'),
(32, 'ELE-005', '前大灯', 30, 8, 'A-06-05', 'ELE-20260301'),
(33, 'ELE-006', '喇叭', 100, 25, 'A-06-06', 'ELE-20260301'),
(34, 'ELE-007', '继电器', 150, 30, 'A-06-07', 'ELE-20260301'),
-- 车身及附件
(35, 'BOD-001', '前车门', 12, 3, 'A-07-01', 'BOD-20260301'),
(36, 'BOD-002', '车窗玻璃', 30, 8, 'A-07-02', 'BOD-20260301'),
(37, 'BOD-003', '安全气囊', 15, 4, 'A-07-03', 'BOD-20260301'),
(38, 'BOD-004', '安全带', 60, 15, 'A-07-04', 'BOD-20260301'),
(39, 'BOD-005', '中控台饰板', 25, 8, 'A-07-05', 'BOD-20260301'),
(40, 'BOD-006', '后视镜', 40, 10, 'A-07-06', 'BOD-20260301');

-- 插入配件数据
-- 一、发动机配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('ENG-001', '缸体', '发动机配件', '潍柴', 'WP10', '标准型', '个', 2800.00, '潍柴动力股份有限公司', '潍柴授权经销商', '发动机核心部件，提供燃烧室和活塞运动空间', 1),
('ENG-002', '缸盖', '发动机配件', '玉柴', 'YC6L', '标准型', '个', 1600.00, '广西玉柴机器股份有限公司', '玉柴授权经销商', '发动机气缸盖，密封气缸上部', 1),
('ENG-003', '活塞', '发动机配件', '马勒', 'Mahle-123', '标准型', '个', 320.00, '马勒集团', '马勒中国代理商', '发动机活塞，将燃烧压力转化为机械功', 1),
('ENG-004', '曲轴', '发动机配件', '康明斯', 'ISF2.8', '标准型', '根', 2200.00, '康明斯（中国）投资有限公司', '康明斯授权经销商', '发动机曲轴，将活塞的往复运动转化为旋转运动', 1),
('ENG-005', '连杆', '发动机配件', '博世', 'Bosch-456', '标准型', '个', 450.00, '博世汽车部件有限公司', '博世中国代理商', '连接活塞和曲轴的部件', 1),
('ENG-006', '进气门', '发动机配件', '日立', 'Hitachi-789', '标准型', '个', 85.00, '日立汽车系统（中国）有限公司', '日立授权经销商', '发动机进气门，控制进气量', 1),
('ENG-007', '凸轮轴', '发动机配件', '三菱', 'Mitsubishi-321', '标准型', '根', 1100.00, '三菱汽车零部件有限公司', '三菱授权经销商', '控制气门开启和关闭的部件', 1),
('ENG-008', '油底壳', '发动机配件', '法雷奥', 'Valeo-654', '标准型', '个', 260.00, '法雷奥集团', '法雷奥中国代理商', '储存发动机机油的部件', 1);

-- 二、传动系配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('TRA-001', '离合器压盘', '传动系配件', '萨克斯', 'Sachs-123', '标准型', '个', 680.00, '萨克斯汽车零部件有限公司', '萨克斯授权经销商', '离合器压盘，传递发动机动力', 1),
('TRA-002', '离合器片', '传动系配件', '卢卡斯', 'Lucas-456', '标准型', '个', 320.00, '卢卡斯汽车零部件有限公司', '卢卡斯授权经销商', '离合器摩擦片，传递动力', 1),
('TRA-003', '变速器总成', '传动系配件', '爱信', 'Aisin-789', '6速手动', '个', 5600.00, '爱信精机（中国）有限公司', '爱信授权经销商', '汽车变速器总成，改变传动比', 1),
('TRA-004', '传动轴', '传动系配件', '德纳', 'Dana-321', '标准型', '根', 1450.00, '德纳（中国）投资有限公司', '德纳授权经销商', '传递动力的轴类部件', 1),
('TRA-005', '差速器', '传动系配件', '博格华纳', 'BorgWarner-654', '标准型', '个', 1800.00, '博格华纳（中国）投资有限公司', '博格华纳授权经销商', '允许左右车轮以不同速度转动', 1);

-- 三、制动系配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('BRA-001', '制动总泵', '制动系配件', '博世', 'Bosch-123', '标准型', '个', 420.00, '博世汽车部件有限公司', '博世中国代理商', '制动系统总泵，提供制动压力', 1),
('BRA-002', '前刹车片', '制动系配件', '菲罗多', 'Ferodo-456', '标准型', '副', 260.00, '菲罗多（中国）有限公司', '菲罗多授权经销商', '前车轮刹车片', 1),
('BRA-003', '后刹车片', '制动系配件', '布雷博', 'Brembo-789', '标准型', '副', 220.00, '布雷博（中国）制动系统有限公司', '布雷博授权经销商', '后车轮刹车片', 1),
('BRA-004', '刹车鼓', '制动系配件', '天合', 'TRW-321', '标准型', '个', 380.00, '天合汽车零部件（中国）有限公司', '天合授权经销商', '鼓式制动器的部件', 1),
('BRA-005', '制动软管', '制动系配件', '盖茨', 'Gates-654', '标准型', '根', 65.00, '盖茨液压技术（苏州）有限公司', '盖茨授权经销商', '传递制动液的软管', 1);

-- 四、转向系配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('STE-001', '转向器', '转向系配件', '采埃孚', 'ZF-123', '液压助力', '个', 1350.00, '采埃孚（中国）投资有限公司', '采埃孚授权经销商', '汽车转向器，控制方向', 1),
('STE-002', '转向节', '转向系配件', '耐世特', 'Nexteer-456', '标准型', '个', 520.00, '耐世特汽车系统（中国）有限公司', '耐世特授权经销商', '连接转向系统的部件', 1),
('STE-003', '主销', '转向系配件', 'TRW', 'TRW-789', '标准型', '个', 130.00, '天合汽车零部件（中国）有限公司', 'TRW授权经销商', '转向节的旋转轴', 1),
('STE-004', '横拉杆', '转向系配件', '德尔福', 'Delphi-321', '标准型', '根', 180.00, '德尔福汽车系统（中国）有限公司', '德尔福授权经销商', '连接转向器和转向节的部件', 1);

-- 五、行驶系配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('RUN-001', '后桥总成', '行驶系配件', '东风德纳', 'DFD-123', '标准型', '个', 3200.00, '东风德纳车桥有限公司', '东风德纳授权经销商', '汽车后桥总成', 1),
('RUN-002', '前悬架总成', '行驶系配件', 'KYB', 'KYB-456', '标准型', '套', 1800.00, 'KYB（中国）投资有限公司', 'KYB授权经销商', '前悬架系统总成', 1),
('RUN-003', '减震器', '行驶系配件', '蒙诺', 'Monroe-789', '标准型', '个', 360.00, '蒙诺减振器（上海）有限公司', '蒙诺授权经销商', '减少振动的部件', 1),
('RUN-004', '轮胎', '行驶系配件', '米其林', 'Michelin-321', '205/55 R16', '条', 680.00, '米其林（中国）投资有限公司', '米其林授权经销商', '汽车轮胎', 1),
('RUN-005', '轮毂', '行驶系配件', '戴卡', 'Dicastal-654', '16英寸', '个', 480.00, '中信戴卡股份有限公司', '戴卡授权经销商', '汽车轮毂', 1);

-- 六、电气仪表及电子配件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('ELE-001', '水温传感器', '电气仪表及电子配件', '博世', 'Bosch-123', '标准型', '个', 95.00, '博世汽车部件有限公司', '博世中国代理商', '检测发动机水温的传感器', 1),
('ELE-002', '氧传感器', '电气仪表及电子配件', '电装', 'Denso-456', '标准型', '个', 280.00, '电装（中国）投资有限公司', '电装授权经销商', '检测排气中氧含量的传感器', 1),
('ELE-003', 'ECU 控制单元', '电气仪表及电子配件', '大陆', 'Continental-789', '标准型', '个', 2600.00, '大陆汽车电子（长春）有限公司', '大陆授权经销商', '发动机控制单元', 1),
('ELE-004', '点火开关', '电气仪表及电子配件', '德尔福', 'Delphi-321', '标准型', '个', 120.00, '德尔福汽车系统（中国）有限公司', '德尔福授权经销商', '汽车点火开关', 1),
('ELE-005', '前大灯', '电气仪表及电子配件', '海拉', 'Hella-654', 'LED', '个', 450.00, '海拉（上海）管理有限公司', '海拉授权经销商', '汽车前大灯', 1),
('ELE-006', '喇叭', '电气仪表及电子配件', '电装', 'Denso-987', '标准型', '个', 45.00, '电装（中国）投资有限公司', '电装授权经销商', '汽车喇叭', 1),
('ELE-007', '继电器', '电气仪表及电子配件', '欧姆龙', 'Omron-654', '标准型', '个', 25.00, '欧姆龙（中国）有限公司', '欧姆龙授权经销商', '电气继电器', 1);

-- 七、车身及附件
INSERT INTO part_info (part_code, part_name, category, brand, model, specification, unit, price, manufacturer, supplier, description, status) VALUES
('BOD-001', '前车门', '车身及附件', '福耀', 'Fuyao-123', '标准型', '个', 1800.00, '福耀玻璃工业集团股份有限公司', '福耀授权经销商', '汽车前车门', 1),
('BOD-002', '车窗玻璃', '车身及附件', '福耀', 'Fuyao-456', '标准型', '块', 320.00, '福耀玻璃工业集团股份有限公司', '福耀授权经销商', '汽车车窗玻璃', 1),
('BOD-003', '安全气囊', '车身及附件', '奥托立夫', 'Autoliv-789', '标准型', '个', 1200.00, '奥托立夫（中国）汽车安全系统有限公司', '奥托立夫授权经销商', '汽车安全气囊', 1),
('BOD-004', '安全带', '车身及附件', '均胜电子', 'Joyson-321', '标准型', '条', 180.00, '宁波均胜电子股份有限公司', '均胜电子授权经销商', '汽车安全带', 1),
('BOD-005', '中控台饰板', '车身及附件', '延锋', 'Yanfeng-654', '标准型', '个', 420.00, '延锋汽车饰件系统有限公司', '延锋授权经销商', '汽车中控台饰板', 1),
('BOD-006', '后视镜', '车身及附件', '法雷奥', 'Valeo-987', '电动', '个', 260.00, '法雷奥集团', '法雷奥中国代理商', '汽车后视镜', 1);
