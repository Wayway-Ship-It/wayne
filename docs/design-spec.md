# AI 智能助手 — 设计规范

## 设计原则

1. **渐进增强**: 本地规则优先，大模型兜底
2. **关注点分离**: 意图识别、业务执行、回复生成各自独立
3. **安全第一**: 敏感配置环境变量化，输入过滤，速率限制
4. **可观测**: 所有 AI 调用记录审计日志

## 意图识别策略

### 两级策略

```
用户输入
    │
    ▼
┌─────────────────────┐
│ 第一级: 本地规则引擎  │ → 命中 → 直接执行
│ - 关键词匹配          │   (~5ms)
│ - 正则表达式          │
└─────────┬───────────┘
          │ 未命中
          ▼
┌─────────────────────┐
│ 第二级: LLM 意图识别  │ → 返回结构化 JSON
│ - LangChain4j 调用    │   intent + params
│ - System Prompt 引导  │   (~500ms-2s)
└─────────────────────┘
```

### 意图类型定义

```java
public enum IntentType {
    GREETING,              // 问候语
    QUERY_STOCK_BY_PART,   // 按配件名查库存
    QUERY_LOW_STOCK,       // 低库存查询
    NAVIGATE_PAGE,         // 页面跳转
    GUIDE_OPERATION,       // 操作指引
    STATISTICS,            // 数据统计
    QUERY_PART_COUNT,      // 配件种类数
    QUERY_CATEGORY_COUNT,  // 分类数量
    QUERY_PENDING_APPROVAL,// 待审批数
    CHAT                   // 纯对话
}
```

## RAG Pipeline 设计

```
知识库文档 (MD)
    │
    ▼
┌──────────────┐
│ 文档加载器     │ 读取 knowledge-base/*.md
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 文档分块器     │ DocumentSplitter (chunk=500, overlap=50)
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 嵌入服务       │ text-embedding-v3 → 向量
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 向量存储       │ ChromaEmbeddingStore
└──────────────┘

查询流程:
用户问题 → 向量化 → 相似度检索(Top-K) → 拼接相关片段 → LLM 生成回答
```

## 对话记忆设计

```
┌────────────────────────────────┐
│ ChatSessionManager             │
│                                │
│ sessions: Map<sessionId,       │
│              ChatMemory>       │
│                                │
│ createSession() → sessionId    │
│ getMemory(sessionId) → Memory  │
│ cleanup(ttl=30min)            │
└────────────────────────────────┘
```

- 每个浏览器 Tab 一个 sessionId
- 记忆窗口: 最近 10 轮对话
- TTL: 30 分钟无活动自动清理

## 响应类型规范

### JSON 响应 (POST /api/ai/chat)
```json
{
  "code": 200,
  "data": {
    "type": "text|navigate",
    "content": "回复文本",
    "route": "/inbound"  // 仅 navigate 类型
  }
}
```

### SSE 响应 (GET /api/ai/chat/stream)
```
data: {"type":"token","content":"前"}
data: {"type":"token","content":"车"}
data: {"type":"token","content":"门"}
...
data: {"type":"done","metadata":{"tokens":42}}
```

## 错误处理规范

| 场景 | HTTP 状态码 | 用户提示 |
|------|-------------|----------|
| 消息为空 | 400 | "请输入您的问题" |
| 速率限制 | 429 | "请求过于频繁，请稍后再试" |
| LLM 超时 | 200 (降级) | "AI 服务响应较慢，请简化问题重试" |
| LLM 错误 | 200 (降级) | "抱歉，AI 服务暂时不可用" |
| 未启用 | 200 | "AI 助手功能暂未启用" |
