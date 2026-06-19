# CLAUDE.md — 汽车配件仓库管理系统开发指南

## 项目简介

这是一个汽车配件仓库管理系统（Auto Parts Warehouse Management System），包含配件管理、出入库管理、库存管理、盘点、供应商/客户管理等模块，以及一个基于大模型的 AI 智能助手。

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, Java 17, MySQL 8.0 |
| 前端 | Vue 2.7.14, Element UI 2.15.13, Vue Router 3.x, Vuex 3.x, Axios |
| AI | LangChain4j 1.0.0-alpha1, 阿里云百炼 Qwen3-Max, Chroma 向量库 |

## 项目结构

```
d:\TRAE\index\
├── CLAUDE.md                          # ← 本文件
├── docs/                              # 📚 项目标准文档
│   ├── requirements.md                #   需求文档 — AI 助手功能需求
│   ├── tech-spec.md                   #   技术规范 — 技术栈、架构、配置
│   ├── design-spec.md                 #   设计规范 — 架构设计、数据流、RAG Pipeline
│   ├── execution-steps.md             #   执行步骤 — 6 阶段开发计划
│   └── api-reference.md               #   API 参考 — 接口文档
├── dev-logs/                          # 📝 每日开发日志
│   └── YYYY-MM-DD.md                  #   格式: 已完成/待办/问题/明日计划
├── auto-parts-warehouse-backend/      # ☕ 后端项目
│   ├── pom.xml
│   └── src/main/java/com/autoparts/warehouse/
│       ├── WarehouseApplication.java  #   入口
│       ├── ai/                        #   🆕 AI 基础设施（开发中）
│       ├── controller/                #   REST 控制器
│       ├── service/                   #   业务服务（含 AIService, RAGService）
│       ├── entity/                    #   数据实体
│       ├── mapper/                    #   MyBatis Mapper
│       └── config/                    #   Spring 配置
└── auto-parts-warehouse-frontend/     # 🖥️ 前端项目
    └── src/
        ├── App.vue                    #   根组件（注册 AIAssistant）
        ├── components/
        │   └── AIAssistant.vue        #   AI 对话组件
        ├── views/                     #   页面视图
        ├── router/                    #   路由配置
        └── store/                     #   Vuex 状态管理
```

## 开发工作流

### 启动项目

```bash
# 1. 确保 MySQL 运行，数据库 auto_parts_warehouse 已创建
mysql -u root -p -e "SHOW DATABASES LIKE 'auto_parts_warehouse';"

# 2. 启动后端 (端口 8080)
cd auto-parts-warehouse-backend
mvn spring-boot:run

# 3. 启动前端 (端口 8081)
cd auto-parts-warehouse-frontend
npm run serve
```

### 测试 AI 接口

```bash
# 状态检查
curl http://localhost:8080/api/ai/status

# 对话测试
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"查前车门库存"}'
```

### 每日开发流程

1. 开发前：阅读 [execution-steps.md](docs/execution-steps.md) 确认当前 Phase
2. 开发中：遵循 [design-spec.md](docs/design-spec.md) 的设计规范
3. 开发后：更新 `dev-logs/` 当日日志
4. 提交前：检查是否符合 [requirements.md](docs/requirements.md) 的需求

## AI 助手开发规范

### 必须遵守

1. **安全第一**: API Key 和密码不得硬编码，使用环境变量 `${ENV_VAR}`
2. **渐进增强**: 本地规则优先（< 5ms），大模型兜底（~500ms-2s）
3. **降级友好**: LLM 调用失败时不报错，返回友好提示
4. **审计日志**: 所有 LLM 调用需记录请求/响应/Token 数/耗时
5. **知识库分离**: 操作指南放在 `knowledge-base/*.md`，不硬编码在代码中

### AI Package 结构

```
com.autoparts.warehouse.ai/
├── config/          # 配置 Bean
├── client/          # LLM 调用封装
├── rag/             # RAG Pipeline（文档加载、嵌入、检索）
├── memory/          # 对话记忆管理
└── audit/           # 审计和用量统计
```

### 当前状态

- **Phase 0** (基础设施搭建) ✅ 已完成 (2026-06-18)
- **Phase 1** (LangChain4j 升级 & 统一LLM客户端) ✅ 已完成 (2026-06-18)
- **Phase 2** (真正的 RAG — 向量检索) ✅ 已完成 (2026-06-18)
- **Phase 3** (SSE 流式响应 & 对话记忆) ✅ 已完成 (2026-06-18)
- **Phase 4** (前端体验优化) ✅ 已完成 (2026-06-18)
- **全部 6 个 Phase 已完成** ✅ (2026-06-18)
- AI 包提供: `ChatModel`, `StreamingChatModel`, `EmbeddingModel`, `EmbeddingStore`, `ChatMemoryStore` 五个 Bean
- SSE 端点 `GET /api/ai/chat/stream` 支持逐字输出 + 多轮对话
- 前端: SSE流式接收 + Markdown渲染 + 持久化 + 重试 + 登录页隐藏
- 生产加固: 速率限制(20次/分钟) + 审计日志 + `/api/ai/stats` 统计端点
- 详见 [execution-steps.md](docs/execution-steps.md)

### API Key 配置

开发前请设置环境变量：

```bash
export AI_API_KEY="sk-ws-your-key-here"
export AI_MODEL_NAME="qwen3-max"
export SMTP_PASSWORD="your-smtp-password"
```

当前项目使用的 API Key 信息在 `application.yml` 中，**不要提交到 Git**。如果 Key 失效，需要去[阿里云百炼控制台](https://bailian.console.aliyun.com/)重新生成。

## 已知问题

1. InMemoryEmbeddingStore 每次重启需重建索引（后续可切换 Chroma 持久化）
2. System Prompt 模板仍硬编码在 `callLLMForIntent()` 中（建议外部化到 application.yml）
3. Token 计数为估算值（非 API 返回的实际值）
4. 审计日志仅保存在内存中（建议后续接入持久化存储）

## 参考链接

- [LangChain4j 文档](https://docs.langchain4j.dev/)
- [阿里云百炼 DashScope API](https://help.aliyun.com/zh/model-studio/)
- [Element UI 文档](https://element.eleme.cn/)
