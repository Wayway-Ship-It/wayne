# AI 智能助手 — API 参考

## 基础地址

- 开发环境: `http://localhost:8080`
- 生产环境: 由部署配置决定

## 认证

所有 AI API 需要 JWT Token（通过 Spring Security 认证）。

```
Authorization: Bearer <token>
```

---

## 1. AI 对话（JSON 模式）

### POST /api/ai/chat

发送消息并获取完整回复。

**请求体：**
```json
{
  "message": "查一下前车门库存"
}
```

**成功响应 (200):**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "type": "text",
    "content": "【前车门】当前库存：21 个，安全库存：3 个\n"
  }
}
```

**导航响应 (200):**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "type": "navigate",
    "route": "/inbound",
    "content": "正在为您打开入库管理页面..."
  }
}
```

**错误响应 (400):**
```json
{
  "code": 400,
  "message": "请输入您的问题",
  "data": null
}
```

**限流响应 (429):**
```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

---

## 2. AI 对话（流式模式）

### GET /api/ai/chat/stream

通过 Server-Sent Events 流式接收回复。

**查询参数：**
| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| message | string | 是 | 用户消息 |
| sessionId | string | 否 | 会话 ID，用于多轮对话 |

**SSE 事件：**

```
data: {"type":"token","content":"【"}

data: {"type":"token","content":"前"}

data: {"type":"token","content":"车"}

data: {"type":"token","content":"门"}

...

data: {"type":"navigate","route":"/inbound"}

data: {"type":"done","metadata":{"tokens":42,"model":"qwen3-max"}}
```

**事件类型：**

| type | 描述 |
|------|------|
| token | 文本片段 |
| navigate | 导航指令 |
| error | 错误信息 |
| done | 对话结束 |

---

## 3. AI 助手状态

### GET /api/ai/status

**成功响应 (200):**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "enabled": true,
    "hasExternalLLM": true
  }
}
```

---

## 意图参考

| 用户输入示例 | 匹配意图 | 返回类型 |
|-------------|---------|---------|
| 你好 / 在吗 | greeting | text |
| 查刹车片库存 | query_stock_by_part | text |
| 哪些配件库存不足 | query_low_stock | text |
| 打开入库单页面 | navigate_page | navigate |
| 怎么新增供应商 | guide_operation | text |
| 本月入库总数量 | statistics | text |
| 有多少种配件 | query_part_count | text |
| 有多少待审批的出库单 | query_pending_approval | text |

---

## 前端集成示例

### 基础调用
```javascript
const res = await this.$axios.post('/api/ai/chat', {
  message: '查前车门库存'
})

if (res.code === 200) {
  if (res.data.type === 'navigate' && res.data.route) {
    this.$router.push(res.data.route)
  }
  console.log(res.data.content)
}
```

### SSE 流式调用
```javascript
const url = `/api/ai/chat/stream?message=${encodeURIComponent(msg)}`
const eventSource = new EventSource(url)

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data)
  if (data.type === 'token') {
    this.appendToken(data.content)
  } else if (data.type === 'done') {
    eventSource.close()
  }
}
```
