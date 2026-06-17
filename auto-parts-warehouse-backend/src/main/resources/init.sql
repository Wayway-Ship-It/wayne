-- 创建供应商表
CREATE TABLE IF NOT EXISTS supplier (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NULL,
  phone VARCHAR(20) NOT NULL,
  contact_person VARCHAR(50) NULL,
  email VARCHAR(100) NULL,
  description TEXT NULL,
  status INT NULL DEFAULT 1,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted INT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- 创建客户表
CREATE TABLE IF NOT EXISTS customer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(255) NULL,
  phone VARCHAR(20) NOT NULL,
  contact_person VARCHAR(50) NULL,
  email VARCHAR(100) NULL,
  description TEXT NULL,
  status INT NULL DEFAULT 1,
  create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted INT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- 插入供应商数据
INSERT INTO supplier (name, phone, status) VALUES
('潍柴授权经销商', '13800138001', 1),
('玉柴授权经销商', '13800138002', 1),
('马勒中国代理商', '13800138003', 1),
('康明斯授权经销商', '13800138004', 2),
('博世中国代理商', '13800138005', 1),
('日立授权经销商', '13800138006', 1),
('三菱授权经销商', '13800138007', 1),
('法雷奥中国代理商', '13800138008', 1),
('萨克斯授权经销商', '13800138009', 1),
('卢卡斯授权经销商', '13800138010', 1),
('爱信授权经销商', '13800138011', 1),
('德纳授权经销商', '13800138012', 1),
('博格华纳授权经销商', '13800138013', 1),
('菲罗多授权经销商', '13800138014', 1),
('布雷博授权经销商', '13800138015', 1),
('天合授权经销商', '13800138016', 1),
('盖茨授权经销商', '13800138017', 1),
('采埃孚授权经销商', '13800138018', 1),
('耐世特授权经销商', '13800138019', 1),
('TRW授权经销商', '13800138020', 1),
('德尔福授权经销商', '13800138021', 1),
('东风德纳授权经销商', '13800138022', 1),
('KYB授权经销商', '13800138023', 1),
('蒙诺授权经销商', '13800138024', 1),
('米其林授权经销商', '13800138025', 1),
('戴卡授权经销商', '13800138026', 1),
('电装授权经销商', '13800138027', 1),
('大陆授权经销商', '13800138028', 1),
('海拉授权经销商', '13800138029', 1),
('欧姆龙授权经销商', '13800138030', 1),
('福耀授权经销商', '13800138031', 1),
('奥托立夫授权经销商', '13800138032', 1),
('均胜电子授权经销商', '13800138033', 1),
('延锋授权经销商', '13800138034', 1),
('法雷奥集团', '13800138035', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  phone = VALUES(phone),
  status = VALUES(status);

-- 插入客户数据
INSERT INTO customer (name, phone, status) VALUES
('北京汽车维修服务有限公司', '13900139001', 1),
('上海汽车配件销售公司', '13900139002', 1),
('广州汽车维修连锁', '13900139003', 1),
('深圳汽车配件贸易公司', '13900139004', 1),
('成都汽车服务有限公司', '13900139005', 1),
('武汉汽车维修中心', '13900139006', 1),
('西安汽车配件销售中心', '13900139007', 1),
('南京汽车服务连锁', '13900139008', 1),
('杭州汽车维修有限公司', '13900139009', 1),
('重庆汽车配件贸易中心', '13900139010', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  phone = VALUES(phone),
  status = VALUES(status);