# AI 智能助手 — 执行步骤

## 概述

本文档定义了 AI 智能助手从当前原型到生产级实现的完整开发路线图。

开发原则：**增量迭代、每步可验证、不破坏现有功能**。

---

## Phase 0: 基础设施搭建 ✅ 已完成 (2026-06-18)

1. 创建 `docs/` 文件夹及标准文档
2. 创建 `dev-logs/` 文件夹及每日日志模板
3. 创建 `CLAUDE.md` 项目指南文件
4. 安全修复：API Key 迁移到环境变量
5. 创建 `ai/` package 基础结构
6. 清理 AIService 死代码

---

## Phase 1: 基础设施清理 & LangChain4j 升级 ✅ 已完成 (2026-06-18)

### 任务清单

- [x] 升级 `pom.xml`: langchain4j 0.27.0 → **1.0.0** (stable)
- [x] 升级 `pom.xml`: java.version 1.8 → **17**
- [x] 删除 `AIService.recognizeIntent()` (死代码, ~90行)
- [x] 删除 `AIService.callExternalLLM()` (孤立方法, ~60行)
- [x] 删除 `AIService.IntentResult` 内部类
- [x] 删除 `RAGService.needsRAG()` (未使用)
- [x] 创建 `ai/RestTemplateConfig.java` — HTTP 超时配置
- [x] 创建 `ai/AIConfigProperties.java` — @ConfigurationProperties
- [x] 用 Jackson `ObjectMapper` 替换 `parseLLMResult` 手写 JSON 解析
- [x] 修复 `RAGService` 知识库路径为 classpath 加载
- [x] **额外完成**: 创建 `ai/config/DashScopeConfig.java` — ChatModel Bean (对接 DashScope OpenAI 兼容接口)
- [x] **额外完成**: 创建 `ai/client/LLMClient.java` — 统一 LLM 调用封装 (降级+异常处理)
- [x] **额外完成**: 重构 `AIService.callLLMForIntent()` → `llmClient.chatWithSystem()`
- [x] **额外完成**: 重构 `RAGService.callLLM()` → `llmClient.chat()`
- [x] **额外完成**: 移除 AIService/RAGService 中的 `apiKey`/`apiUrl`/`modelName` 硬编码字段

### 关键变更
- `ChatLanguageModel` → `ChatModel` (langchain4j 1.0.0 API 重命名)
- `Response<AiMessage>` → `ChatResponse` (返回类型变更)
- DashScope 集成方式：`langchain4j-open-ai` 模块 + 自定义 `baseUrl` 指向阿里云百炼

### 验证
```bash
mvn clean compile  # ✅ BUILD SUCCESS
curl /api/ai/chat   # ✅ 所有意图识别正常工作
```

---

## Phase 2: 真正的 RAG — 向量检索增强生成 ✅ 已完成 (2026-06-18)

### 任务清单

- [x] 创建 `ai/rag/VectorStoreService.java` — 文档加载、分块、嵌入、检索一站式服务
- [x] 创建 `DashScopeConfig.embeddingModel()` Bean — text-embedding-v3
- [x] 创建 `DashScopeConfig.embeddingStore()` Bean — InMemoryEmbeddingStore<TextSegment>
- [x] 实现文档分块 pipeline: load → split → embed(batch≤8) → store
- [x] 实现语义检索: query → embed → search(Top-K) → inject → LLM
- [x] 重写 `RAGService.query()` 使用新 pipeline（VectorStoreService + LLMClient）
- [x] 扩展知识库文档：5 个 MD 文件（system_manual, supplier_management, inventory_management, approval_workflow, warehouse_operations）

### 关键变更
- DashScope 嵌入 API 限制 batch ≤ 10，手动分批嵌入（每批 8 个段落）
- 使用 `DocumentByParagraphSplitter(500, 50)` 按段落分块
- 向量检索 minScore=0.3, TopK=3
- InMemory 存储（后续可切换 Chroma 持久化）

### 验证
```bash
# ✅ "怎么新增供应商" → RAG 检索到 supplier_management.md → LLM 生成分步回答
# ✅ "库存预警怎么设置" → RAG 检索到 inventory_management.md → LLM 生成详细指南
# ✅ "出库审批流程是怎样的" → RAG 检索到 approval_workflow.md → LLM 生成完整流程
# ✅ mvn compile: BUILD SUCCESS (62 source files)
```

---

## Phase 3: SSE 流式响应 & 对话记忆 ✅ 已完成 (2026-06-18)

### 任务清单

- [x] 新增 `GET /api/ai/chat/stream?message=xxx&sessionId=xxx` SSE 端点
- [x] 集成 LangChain4j `OpenAiStreamingChatModel` + `StreamingChatResponseHandler`
- [x] 创建 `DashScopeConfig.streamingChatModel()` Bean
- [x] 创建 `DashScopeConfig.chatMemoryStore()` Bean — InMemoryChatMemoryStore
- [x] 创建 `ai/memory/ChatSessionManager.java` — 多轮对话记忆管理
- [x] 前端增加 `EventSource` SSE 流式接收（token/result/done 事件）
- [x] 前端引入 `marked` 库渲染 AI 回复 Markdown
- [x] 前端增加 localStorage 持久化聊天记录
- [x] 前端新增清空对话按钮 + Shift+Enter 换行支持
- [x] 优化：SSE 端点本地匹配快速路径，避免双重 LLM 调用

### 关键变更
- `StreamingChatResponseHandler` 接口方法: `onPartialResponse` / `onCompleteResponse` / `onError`
- SSE 事件类型: `token`(逐字), `result`(直接结果), `done`(完成), `error`(错误)
- `tryLocalMatch()` 轻量级本地意图匹配，跳过 LLM 快速返回
- 前端 sessionId 生成 + localStorage 持久化

### 验证
```bash
# ✅ "你好" → 本地匹配 → SSE result 事件
# ✅ "解释一下什么是安全库存" → RAG + 流式 LLM → SSE token 逐字输出
# ✅ mvn compile: BUILD SUCCESS (63 source files)
# ✅ 前端 npm build: DONE
```

---

## Phase 4: 前端体验优化 ✅ 已完成 (2026-06-18)

### 任务清单

- [x] 修复未读数 badge 递增逻辑（面板关闭时正确 +1）
- [x] 添加 30s Axios 超时 + 网络错误重试按钮
- [x] 聊天记录 localStorage 持久化（已在 Phase 3 实现）
- [x] 登录页隐藏 AI 悬浮按钮（`v-if="$route.name !== 'Login'"`）
- [x] 创建 `.env.development` 文件管理后端 URL
- [x] Shift+Enter 换行、Enter 发送（已在 Phase 3 实现）
- [x] 移除 `vue.config.js` 无用的 pathRewrite（`^/api` → `/api` 是 no-op）

### 关键变更
- `main.js`: `axios.defaults.baseURL` → `process.env.VUE_APP_API_BASE_URL` + `timeout: 30000`
- `App.vue`: `<AIAssistant v-if="$route.name !== 'Login'" />`
- `vue.config.js`: 移除冗余 pathRewrite
- `AIAssistant.vue`: 新增重试按钮、错误消息标记、`hasError` 计算属性

### 验证
- ✅ 刷新页面，聊天记录保留
- ✅ 登录页 AI 按钮已隐藏
- ✅ `.env.development` 管理 API URL
- ✅ 前端 npm build: DONE

---

## Phase 5: 生产加固 ✅ 已完成 (2026-06-18)

### 任务清单

- [x] 创建 `ai/audit/RateLimitService.java` — 滑动窗口速率限制（按 sessionId）
- [x] 创建 `ai/audit/AIAuditService.java` — LLM 调用审计日志
- [x] 添加估算 token 计数（中文字符 / 1.5 + 英文 / 4.0）
- [x] 新增 `GET /api/ai/stats` 端点 — 审计统计 + 限流状态 + 最近记录
- [x] 增强 `GET /api/ai/status` 端点 — 合并审计统计
- [x] 限流错误返回 HTTP 429 + "请求过于频繁" 提示
- [x] 控制台输出结构化审计日志 `[AI-Audit]`
- [x] 内存保存最近 500 条审计记录（可查询）

### 关键变更
- 速率限制：20 次/分钟/会话，滑动窗口算法，自动清理过期窗口
- 审计记录字段：id, timestamp, sessionId, query, response, tokensUsed, durationMs, success
- `/api/ai/stats` 返回: audit 统计 + rateLimit 状态 + recentRecords(最近10条)
- `RateLimitService.tryAcquire(sessionId)` 在两种端点入口调用

### 验证
```bash
# ✅ 3 次请求 → totalCalls=3, currentCount=3, remaining=17
# ✅ recentRecords 返回完整审计记录
# ✅ successRate: 100.0%
# ✅ mvn compile: BUILD SUCCESS (65 source files)
```
