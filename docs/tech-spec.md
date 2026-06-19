# AI 智能助手 — 技术规范

## 技术栈

### 后端
| 组件 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 2.7.18 | 应用框架 |
| MyBatis-Plus | 3.5.3.1 | ORM |
| MySQL | 8.0 | 业务数据库 |
| LangChain4j | 1.0.0-alpha1 | AI 编排框架 |
| Chroma | (嵌入式) | 向量存储 |

### 前端
| 组件 | 版本 | 用途 |
|------|------|------|
| Vue | 2.7.14 | UI 框架 |
| Element UI | 2.15.13 | 组件库 |
| Axios | 1.4.0 | HTTP 客户端 |
| marked | latest | Markdown 渲染 |

### 外部服务
| 服务 | 模型 | 用途 |
|------|------|------|
| 阿里云百炼 DashScope | qwen3-max | 对话生成 |
| 阿里云百炼 DashScope | text-embedding-v3 | 文本向量化 |

## 架构分层

```
┌─────────────────────────────────────────┐
│  前端 (Vue.js)                           │
│  AIAssistant.vue → EventSource (SSE)     │
├─────────────────────────────────────────┤
│  控制器层 (Spring MVC)                   │
│  AIController (REST + SSE)              │
├─────────────────────────────────────────┤
│  服务层                                  │
│  AIService (意图识别、业务编排)           │
│  RAGService (文档检索、知识增强)          │
│  ChatSessionManager (对话管理)           │
├─────────────────────────────────────────┤
│  AI 基础设施层                            │
│  LLMClient (LangChain4j 封装)           │
│  EmbeddingService (向量化)              │
│  VectorStore (Chroma)                   │
├─────────────────────────────────────────┤
│  数据层                                  │
│  MySQL (业务数据)                        │
│  Chroma (向量数据)                       │
│  知识库文件 (MD 文档)                     │
└─────────────────────────────────────────┘
```

## AI Package 结构

```
com.autoparts.warehouse.ai/
├── config/
│   ├── AIConfigProperties.java    # @ConfigurationProperties
│   ├── DashScopeConfig.java       # LangChain4j Model Beans
│   └── RestTemplateConfig.java    # HTTP 超时配置
├── client/
│   └── LLMClient.java             # 统一 LLM 调用封装
├── rag/
│   ├── DocumentLoader.java        # 知识库文档加载
│   ├── EmbeddingService.java      # 向量嵌入服务
│   └── VectorStoreService.java    # 向量存储服务
├── memory/
│   └── ChatSessionManager.java    # 对话历史管理
└── audit/
    ├── AIAuditService.java        # 审计日志
    └── TokenUsageService.java     # Token 用量统计
```

## API 端点

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/ai/chat` | 发送消息（JSON 请求/响应） |
| GET | `/api/ai/chat/stream` | 发送消息（SSE 流式响应） |
| GET | `/api/ai/status` | 获取 AI 助手状态 |

## 配置规范

所有敏感配置使用环境变量：

```yaml
ai:
  enabled: ${AI_ENABLED:true}
  api-key: ${AI_API_KEY:}
  api-url: ${AI_API_URL:https://dashscope.aliyuncs.com/compatible-mode/v1}
  model-name: ${AI_MODEL_NAME:qwen3-max}
  embedding-model: ${AI_EMBEDDING_MODEL:text-embedding-v3}
  timeout:
    connect: ${AI_CONNECT_TIMEOUT:5000}
    read: ${AI_READ_TIMEOUT:30000}
  rate-limit:
    max-per-minute: ${AI_RATE_LIMIT:20}
```
