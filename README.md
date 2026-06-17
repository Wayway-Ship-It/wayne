# 汽车配件仓库管理系统

## 项目简介

汽车配件仓库管理系统是一个基于前后端分离架构的仓库管理系统，采用Vue2 + Element UI作为前端技术栈，后端使用Spring Boot + MyBatis Plus，数据库采用MySQL 8.0。

## 📋 目录

- [快速开始](#-快速开始)
- [技术栈](#-技术栈)
- [系统功能](#-系统功能)
- [常见问题](#-常见问题)
- [启动脚本](#-启动脚本)

## 🚀 快速开始

### 第一步：环境检查

双击运行 `环境检查.bat`，检查你的系统是否满足要求：
- ✅ JDK 1.8+
- ✅ MySQL 8.0+
- ✅ Maven 3.6+（后端编译需要）
- ✅ Node.js 14+（前端需要）

### 第二步：数据库配置

1. 启动MySQL服务
2. 使用Navicat或命令行创建数据库并执行 `database/init.sql`
3. 修改 `auto-parts-warehouse-backend/src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    username: root      # 改为你的MySQL用户名
    password: your_password  # 改为你的MySQL密码
```

### 第三步：启动服务

**方式一：使用启动脚本（推荐Windows用户）**
1. 双击 `启动后端.bat` 启动后端服务
2. 后端启动成功后，双击 `启动前端.bat` 启动前端服务

**方式二：手动启动**
```bash
# 启动后端
cd auto-parts-warehouse-backend
mvn clean install
mvn spring-boot:run

# 启动前端（新终端）
cd auto-parts-warehouse-frontend
npm install
npm run serve
```

### 第四步：访问系统

- 前端地址：http://localhost:8081
- 后端API：http://localhost:8080

**默认登录账户：**
- 管理员：`admin` / `admin123`
- 普通用户：`user` / `admin123`

## 🛠️ 技术栈

### 前端技术栈
- Vue 2.7.14
- Vue Router 3.6.5
- Vuex 3.6.2
- Element UI 2.15.13
- Axios 1.4.0

### 后端技术栈
- Java 1.8+
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Spring Security
- JWT

### 开发工具
- IntelliJ IDEA（后端开发）
- VS Code（前端开发）
- Navicat for MySQL（数据库管理）

## 💡 系统功能

### 1. 用户管理模块
- 用户注册/登录
- 用户信息CRUD
- 角色管理（管理员/普通用户）
- 权限控制

### 2. 配件信息管理模块
- 配件录入、分类、查询、修改、删除
- 支持多条件搜索

### 3. 入库管理模块
- 入库单创建
- 库存自动增加
- 入库记录查询

### 4. 出库管理模块
- 出库申请、审批流程
- 库存自动扣减
- 实时审批状态跟踪

### 5. 库存管理模块
- 库存查询、预警
- 盘点计划制定
- 盘点数据录入

## ❓ 常见问题

### 服务不可用怎么办？

1. **先运行 `环境检查.bat`** 确认所有环境都已安装
2. **检查MySQL服务** 是否启动
3. **确认数据库** `auto_parts_warehouse` 已创建并执行了init.sql
4. **检查配置文件** `application.yml` 中的数据库连接信息是否正确
5. **查看端口** 8080和8081是否被占用

详细故障排除请查看 [启动说明.md](启动说明.md)

### Maven依赖下载慢？

配置Maven使用阿里云镜像：
1. 打开Maven的 `settings.xml`
2. 添加以下镜像配置：
```xml
<mirror>
  <id>aliyunmaven</id>
  <mirrorOf>*</mirrorOf>
  <name>阿里云公共仓库</name>
  <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### npm install 慢？

使用淘宝镜像：
```bash
npm install --registry=https://registry.npmmirror.com
```

## 📁 项目结构

```
auto-parts-warehouse/
├── auto-parts-warehouse-backend/     # 后端项目 (Spring Boot)
│   ├── src/main/java/com/autoparts/warehouse/
│   │   ├── common/           # 通用类
│   │   ├── config/           # 配置类
│   │   ├── controller/       # 控制器
│   │   ├── dto/              # 数据传输对象
│   │   ├── entity/           # 实体类
│   │   ├── mapper/           # 数据访问层
│   │   ├── service/          # 业务逻辑层
│   │   └── util/             # 工具类
│   └── pom.xml
├── auto-parts-warehouse-frontend/    # 前端项目 (Vue.js)
│   ├── src/
│   │   ├── views/            # 页面组件
│   │   ├── router/           # 路由配置
│   │   └── store/            # Vuex状态管理
│   └── package.json
├── database/                  # 数据库文件
│   └── init.sql
├── 环境检查.bat              # 环境检查脚本
├── 启动后端.bat              # 后端启动脚本
├── 启动前端.bat              # 前端启动脚本
├── 启动说明.md               # 详细启动说明
└── README.md                 # 本文件
```

## 📊 性能指标

- 响应速度：普通查询≤800ms，复杂操作≤2.5s，页面加载≤1.5s
- 数据准确性：库存数据更新延迟≤1s，数据准确率≥99.95%
- 易用性：新用户培训≤30分钟，核心操作≤3步，操作失误率≤1%

## 📝 注意事项

1. 请确保MySQL服务已启动并正常运行
2. 首次运行前请执行数据库初始化脚本
3. 修改配置文件中的数据库连接信息
4. 默认密码仅用于演示，生产环境请修改为强密码

## 📄 许可证

MIT License

