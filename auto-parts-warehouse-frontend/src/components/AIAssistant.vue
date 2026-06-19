<template>
  <div>
    <!-- 悬浮AI图标按钮 -->
    <div
      class="ai-float-btn"
      :class="{ 'ai-float-btn-expanded': isExpanded }"
      @click="togglePanel"
    >
      <i class="el-icon-chat-dot-round"></i>
      <span v-if="unreadCount > 0" class="ai-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <!-- AI对话面板 -->
    <transition name="slide">
      <div v-if="isExpanded" class="ai-panel">
        <!-- 面板头部 -->
        <div class="ai-panel-header">
          <div class="ai-title">
            <i class="el-icon-chat-dot-round ai-icon"></i>
            <span>智能助手</span>
          </div>
          <div class="ai-header-actions">
            <button class="ai-clear-btn" title="清空对话" @click="clearMessages">
              <i class="el-icon-delete"></i>
            </button>
            <button class="ai-close-btn" @click="togglePanel">
              <i class="el-icon-close"></i>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="ai-message-list" ref="messageList">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="ai-message-item"
            :class="{ 'ai-message-user': msg.type === 'user', 'ai-message-ai': msg.type === 'ai' }"
          >
            <div class="ai-avatar">
              <i v-if="msg.type === 'user'" class="el-icon-user"></i>
              <i v-else class="el-icon-chat-dot-round"></i>
            </div>
            <div class="ai-bubble">
              <div v-if="msg.type === 'ai' && msg.streaming" class="ai-loading">
                <span class="ai-dot"></span>
                <span class="ai-dot"></span>
                <span class="ai-dot"></span>
              </div>
              <!-- 错误消息（可重试） -->
              <div v-else-if="msg.type === 'ai' && msg.isError" class="ai-error-msg">
                <span>{{ msg.content }}</span>
              </div>
              <!-- Markdown 渲染区域 -->
              <div
                v-else-if="msg.type === 'ai'"
                class="ai-markdown"
                v-html="renderMarkdown(msg.content)"
              ></div>
              <!-- 用户消息 -->
              <div v-else class="ai-user-text">{{ msg.content }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="ai-input-area">
          <textarea
            ref="inputBox"
            v-model="inputMessage"
            class="ai-input"
            placeholder="请输入您的问题，如：查一下前车门库存"
            @keydown="handleKeydown"
            :disabled="isLoading"
          ></textarea>
          <button
            v-if="hasError"
            class="ai-retry-btn"
            title="重新发送上一条消息"
            @click="retryLastMessage"
          >
            <i class="el-icon-refresh"></i>
          </button>
          <button
            class="ai-send-btn"
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isLoading"
          >
            <i class="el-icon-send"></i>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { marked } from 'marked'

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true
})

export default {
  name: 'AIAssistant',
  data() {
    return {
      isExpanded: false,
      isLoading: false,
      inputMessage: '',
      messages: [],
      unreadCount: 0,
      lastUserMessage: '',
      sessionId: 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9),
      eventSource: null
    }
  },
  computed: {
    hasError() {
      const last = this.messages[this.messages.length - 1]
      return last && last.type === 'ai' && last.isError
    }
  },
  mounted() {
    // 初始化欢迎消息
    this.addAiMessage('您好！我是您的智能助手，请问有什么可以帮您的？\n\n我可以帮您：\n- 查询配件库存（如：查前车门库存）\n- 打开功能页面（如：打开入库单页面）\n- 提供操作指引（如：怎么新增供应商）\n- 统计数据查询（如：本月入库多少）')

    // 从 localStorage 恢复聊天记录
    this.loadMessages()
  },
  beforeDestroy() {
    this.closeEventSource()
  },
  watch: {
    // 面板关闭时，后续 AI 回复计入未读
    isExpanded(val) {
      if (val) {
        this.unreadCount = 0
      }
    }
  },
  methods: {
    /**
     * 重新发送上一条消息（网络错误时重试）
     */
    retryLastMessage() {
      if (!this.hasError || !this.lastUserMessage) return
      // 移除最近的错误消息
      this.messages.pop()
      // 重新发送
      this.inputMessage = this.lastUserMessage
      this.sendMessage()
    },

    /**
     * 渲染 Markdown
     */
    renderMarkdown(text) {
      if (!text) return ''
      try {
        return marked.parse(text)
      } catch {
        return text.replace(/\n/g, '<br>')
      }
    },

    /**
     * 切换面板展开/收起
     */
    togglePanel() {
      this.isExpanded = !this.isExpanded
      if (this.isExpanded) {
        this.unreadCount = 0
        this.$nextTick(() => this.scrollToBottom())
      }
    },

    /**
     * 清空对话历史
     */
    clearMessages() {
      this.messages = []
      this.sessionId = 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9)
      localStorage.removeItem('ai_messages')
      localStorage.removeItem('ai_sessionId')
      this.addAiMessage('对话已清空，有什么可以帮您的？')
    },

    /**
     * 键盘事件处理
     * Enter 发送，Shift+Enter 换行
     */
    handleKeydown(e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault()
        this.sendMessage()
      }
    },

    /**
     * 发送消息（SSE 流式）
     */
    async sendMessage() {
      const message = this.inputMessage.trim()
      if (!message || this.isLoading) return

      // 添加用户消息
      this.addUserMessage(message)
      this.lastUserMessage = message
      this.inputMessage = ''
      this.isLoading = true
      this.$refs.inputBox.style.height = 'auto'

      // 添加 AI 占位消息（流式更新）
      const aiMsgIndex = this.messages.length
      this.messages.push({
        type: 'ai',
        content: '',
        streaming: true
      })
      this.$nextTick(() => this.scrollToBottom())

      // 关闭之前的 EventSource
      this.closeEventSource()

      // 构建 SSE URL
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      const url = `${baseUrl}/api/ai/chat/stream?message=${encodeURIComponent(message)}&sessionId=${encodeURIComponent(this.sessionId)}`

      try {
        this.eventSource = new EventSource(url)
        let hasStreamContent = false

        // 接收流式 token
        this.eventSource.addEventListener('token', (event) => {
          hasStreamContent = true
          this.messages[aiMsgIndex].streaming = false
          this.messages[aiMsgIndex].content += event.data
          this.$nextTick(() => this.scrollToBottom())
        })

        // 直接结果（非流式意图：库存查询、导航、统计等）
        this.eventSource.addEventListener('result', (event) => {
          this.closeEventSource()
          this.isLoading = false
          try {
            const result = JSON.parse(event.data)
            if (result.code === 200 && result.data) {
              const data = result.data
              this.messages[aiMsgIndex] = {
                type: 'ai',
                content: data.content || JSON.stringify(data),
                streaming: false
              }
              // 导航指令
              if (data.type === 'navigate' && data.route) {
                this.$router.push(data.route)
              }
            }
          } catch (e) {
            this.messages[aiMsgIndex] = {
              type: 'ai',
              content: event.data,
              streaming: false
            }
          }
          this.incrementUnread()
          this.$nextTick(() => this.scrollToBottom())
        })

        // 流式完成
        this.eventSource.addEventListener('done', (event) => {
          this.closeEventSource()
          this.isLoading = false
          this.messages[aiMsgIndex].streaming = false
          this.incrementUnread()
          this.saveMessages()
          this.$nextTick(() => this.scrollToBottom())
        })

        // 错误处理
        this.eventSource.addEventListener('error', (event) => {
          this.closeEventSource()
          this.isLoading = false
          if (!hasStreamContent && this.messages[aiMsgIndex].content === '') {
            this.messages[aiMsgIndex] = {
              type: 'ai',
              content: '抱歉，请求超时或网络错误，请重试。',
              streaming: false,
              isError: true
            }
          }
        })

        // EventSource 默认事件（某些浏览器不支持命名事件）
        this.eventSource.onmessage = (event) => {
          // 命名事件已处理，忽略默认事件
        }

        this.eventSource.onerror = () => {
          this.closeEventSource()
          this.isLoading = false
          if (this.messages[aiMsgIndex].content === '') {
            this.messages[aiMsgIndex] = {
              type: 'ai',
              content: '抱歉，连接失败，请检查网络后重试。',
              streaming: false,
              isError: true
            }
          }
        }

      } catch (error) {
        console.error('SSE 连接失败:', error)
        this.closeEventSource()
        this.isLoading = false
        this.messages[aiMsgIndex] = {
          type: 'ai',
          content: '网络连接失败，请检查网络后重试',
          streaming: false,
          isError: true
        }
      }
    },

    /**
     * 关闭 EventSource
     */
    closeEventSource() {
      if (this.eventSource) {
        this.eventSource.close()
        this.eventSource = null
      }
    },

    /**
     * 增加未读计数（面板关闭时）
     */
    incrementUnread() {
      if (!this.isExpanded) {
        this.unreadCount++
      }
    },

    /**
     * 添加用户消息
     */
    addUserMessage(content) {
      this.messages.push({
        type: 'user',
        content: content
      })
      this.saveMessages()
    },

    /**
     * 添加 AI 消息
     */
    addAiMessage(content) {
      this.messages.push({
        type: 'ai',
        content: content,
        streaming: false
      })
    },

    /**
     * 持久化消息到 localStorage
     */
    saveMessages() {
      try {
        const toSave = this.messages.slice(-50) // 最多保留 50 条
        localStorage.setItem('ai_messages', JSON.stringify(toSave))
        localStorage.setItem('ai_sessionId', this.sessionId)
      } catch (e) {
        // localStorage 满了，忽略
      }
    },

    /**
     * 从 localStorage 恢复消息
     */
    loadMessages() {
      try {
        const saved = localStorage.getItem('ai_messages')
        const savedSessionId = localStorage.getItem('ai_sessionId')
        if (saved) {
          this.messages = JSON.parse(saved)
        }
        if (savedSessionId) {
          this.sessionId = savedSessionId
        }
      } catch (e) {
        // 忽略
      }
    },

    /**
     * 滚动到底部
     */
    scrollToBottom() {
      const messageList = this.$refs.messageList
      if (messageList) {
        messageList.scrollTop = messageList.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
/* 悬浮按钮 */
.ai-float-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: white;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
  z-index: 9999;
}

.ai-float-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.5);
}

.ai-float-btn-expanded {
  transform: rotate(90deg);
}

.ai-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: #F56C6C;
  color: white;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  font-weight: 600;
}

/* 对话面板 */
.ai-panel {
  position: fixed;
  right: 30px;
  bottom: 100px;
  width: 420px;
  max-height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 9998;
  overflow: hidden;
}

/* 面板头部 */
.ai-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
  color: white;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.ai-icon {
  font-size: 20px;
}

.ai-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.ai-clear-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  transition: background 0.2s;
}

.ai-clear-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.ai-close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  transition: background 0.2s;
}

.ai-close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 消息列表 */
.ai-message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  max-height: 450px;
}

.ai-message-list::-webkit-scrollbar {
  width: 6px;
}

.ai-message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.ai-message-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.ai-message-list::-webkit-scrollbar-thumb:hover {
  background: #999;
}

/* 消息项 */
.ai-message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.ai-message-user {
  flex-direction: row-reverse;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-message-user .ai-avatar {
  background: #409EFF;
  color: white;
}

.ai-message-ai .ai-avatar {
  background: #67C23A;
  color: white;
}

.ai-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 18px;
  margin: 0 8px;
  font-size: 14px;
  line-height: 1.6;
}

.ai-message-user .ai-bubble {
  background: #409EFF;
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message-ai .ai-bubble {
  background: #f5f7fa;
  color: #606266;
  border-bottom-left-radius: 4px;
}

/* Markdown 内容样式 */
.ai-markdown >>> h1, .ai-markdown >>> h2, .ai-markdown >>> h3 {
  margin: 8px 0 4px 0;
  font-size: 15px;
  font-weight: 600;
}

.ai-markdown >>> p {
  margin: 4px 0;
}

.ai-markdown >>> ul, .ai-markdown >>> ol {
  padding-left: 18px;
  margin: 4px 0;
}

.ai-markdown >>> li {
  margin: 2px 0;
}

.ai-markdown >>> code {
  background: #e8e8e8;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 13px;
}

.ai-markdown >>> pre {
  background: #2d2d2d;
  color: #f8f8f2;
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 6px 0;
}

.ai-markdown >>> pre code {
  background: none;
  padding: 0;
}

.ai-markdown >>> blockquote {
  border-left: 3px solid #409EFF;
  padding-left: 12px;
  margin: 6px 0;
  color: #909399;
}

.ai-markdown >>> strong {
  font-weight: 600;
}

.ai-markdown >>> table {
  border-collapse: collapse;
  margin: 6px 0;
  font-size: 13px;
}

.ai-markdown >>> th, .ai-markdown >>> td {
  border: 1px solid #dcdfe6;
  padding: 4px 8px;
  text-align: left;
}

.ai-markdown >>> th {
  background: #f5f7fa;
  font-weight: 600;
}

/* 用户消息纯文本 */
.ai-user-text {
  white-space: pre-wrap;
  word-break: break-word;
}

/* 加载动画 */
.ai-loading {
  display: flex;
  gap: 6px;
  padding: 8px 12px;
}

.ai-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  animation: bounce 1.4s infinite ease-in-out both;
}

.ai-dot:nth-child(1) { animation-delay: -0.32s; }
.ai-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 输入区域 */
.ai-input-area {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.ai-input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  resize: none;
  font-size: 14px;
  line-height: 1.4;
  min-height: 40px;
  max-height: 120px;
  overflow-y: auto;
  transition: border-color 0.2s;
  font-family: inherit;
}

.ai-input:focus {
  outline: none;
  border-color: #409EFF;
}

.ai-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.ai-send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409EFF;
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.ai-send-btn:hover:not(:disabled) {
  background: #66b1ff;
  transform: scale(1.05);
}

.ai-send-btn:disabled {
  background: #dcdfe6;
  cursor: not-allowed;
}

/* 重试按钮 */
.ai-retry-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #F56C6C;
  border: none;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.ai-retry-btn:hover {
  background: #f89898;
  transform: scale(1.05);
}

/* 错误消息样式 */
.ai-error-msg {
  color: #F56C6C;
  font-size: 13px;
}

/* 过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
