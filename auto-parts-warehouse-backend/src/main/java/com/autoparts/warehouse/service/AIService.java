package com.autoparts.warehouse.service;

import com.autoparts.warehouse.ai.client.LLMClient;
import com.autoparts.warehouse.ai.tools.WarehouseTools;
import com.autoparts.warehouse.ai.tools.WarehouseTools.ToolCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI助手服务层 — Prompt-Based Function Calling
 *
 * 由于 DashScope 不原生支持 OpenAI Function Calling（tool_calls 协议），
 * 改用 Prompt 描述工具集 + LLM 返回 JSON 调用指令的方式实现结构化查询。
 *
 * 流程：
 * 1. 构建 System Prompt（角色设定 + 可用工具列表 + 调用格式）
 * 2. LLM 决定：直接回复（问候/闲聊）或返回 JSON 工具调用
 * 3. 如果返回 JSON 工具调用 → WarehouseTools.dispatch() 执行 → 返回结果
 * 4. 如果返回普通文本 → 直接返回给用户
 *
 * 对比旧方案（120行 if-else 关键词匹配）：
 * - LLM 理解语义，不受关键词变化影响
 * - "火花塞还有多少" / "查火花塞库存" / "火花塞数量" → 都正确路由到 queryStockByPart
 * - 新增工具只需在 TOOL_DEFINITIONS 和 WarehouseTools.dispatch() 中各加一条
 */
@Service
public class AIService {

    @Autowired
    private LLMClient llmClient;

    @Autowired
    private WarehouseTools tools;

    @Value("${ai.enabled:true}")
    private boolean aiEnabled;

    private static final String SYSTEM_PROMPT = """
        你是汽车配件仓库管理系统的 AI 智能助手。
        你的任务是理解用户的自然语言查询并给出准确回复。

        回复规则：
        1. 问候/闲聊/感谢 → 直接友好回复，使用中文
        2. 需要查询数据 → 从【可用工具列表】选择最合适的工具，返回 JSON：
           {"tool":"工具名","params":{"参数名":"值"}}
           只返回 JSON，不要其他文字！
        3. 如果用户问题涉及配件名称，提取纯粹的配件名（去掉"查"、"还有多少"、"有几个"等修饰词）

        """ + WarehouseTools.TOOL_DEFINITIONS;

    /**
     * 处理用户消息 — Prompt-Based Function Calling
     */
    public Map<String, Object> processMessage(String message) {
        if (!aiEnabled) {
            return error("AI助手功能暂未启用，请联系管理员配置。");
        }
        if (!llmClient.isAvailable()) {
            return error("AI助手暂不可用，请检查 API Key 配置。\n\n"
                    + "您可以尝试：\n• 查询配件库存\n• 打开功能页面\n• 查看操作指引");
        }

        try {
            long start = System.currentTimeMillis();

            // 1. 调用 LLM（带上工具定义的 System Prompt）
            String llmResponse = llmClient.chatWithSystem(SYSTEM_PROMPT, message);

            long elapsed = System.currentTimeMillis() - start;

            if (llmResponse == null || llmResponse.isEmpty()) {
                return error("AI 服务返回为空，请稍后重试。");
            }

            // 2. 尝试解析为工具调用
            ToolCall call = tools.extractToolCall(llmResponse);
            if (call != null) {
                System.out.printf("[AI-FuncCall] tool=%s params=%s llmTime=%dms%n",
                        call.toolName(), call.params(), elapsed);

                // 3. 执行工具
                long toolStart = System.currentTimeMillis();
                String toolResult = tools.dispatch(call.toolName(), call.params());
                long toolElapsed = System.currentTimeMillis() - toolStart;
                System.out.printf("[AI-FuncCall] tool=%s result=%s toolTime=%dms totalTime=%dms%n",
                        call.toolName(),
                        toolResult.substring(0, Math.min(80, toolResult.length())).replace("\n", " "),
                        toolElapsed, System.currentTimeMillis() - start);

                return buildResponse(toolResult);
            }

            // 4. 直接文本回复（问候/闲聊）
            return buildResponse(llmResponse);

        } catch (Exception e) {
            System.err.println("AIService 处理失败: " + e.getMessage());
            return error("抱歉，处理您的问题时出现了错误，请稍后再试。");
        }
    }

    /**
     * 本地快速匹配（用于 SSE 流式端点）
     * 仅处理最明显的问候语，跳过 LLM 调用
     */
    public Map<String, Object> tryLocalMatch(String message) {
        String trimmed = message.trim();
        if (trimmed.length() <= 3 && (trimmed.equals("你好") || trimmed.equals("您好")
                || trimmed.equals("在吗") || trimmed.equals("嗨") || trimmed.equals("哈喽"))) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "text");
            response.put("content", "您好！我是您的智能助手，请问有什么可以帮您的？\n\n"
                    + "我可以帮您：\n• 查询配件库存\n• 打开功能页面\n• 提供操作指引\n• 统计数据查询");
            return response;
        }
        return null;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", aiEnabled);
        status.put("hasExternalLLM", llmClient.hasApiKey());
        status.put("mode", "Prompt-Based Function Calling");
        return status;
    }

    /**
     * 构建响应 Map
     */
    private Map<String, Object> buildResponse(String text) {
        Map<String, Object> response = new HashMap<>();

        // 导航指令
        if (text.startsWith("NAVIGATE:")) {
            String payload = text.substring(9);
            String[] parts = payload.split("\\|", 2);
            response.put("type", "navigate");
            response.put("route", parts[0]);
            response.put("content", parts.length > 1 ? parts[1] : "正在为您打开页面...");
            return response;
        }

        response.put("type", "text");
        response.put("content", text);
        return response;
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "text");
        response.put("content", message);
        return response;
    }
}
