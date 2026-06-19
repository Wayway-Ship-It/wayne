package com.autoparts.warehouse.ai.tools;

import com.autoparts.warehouse.entity.*;
import com.autoparts.warehouse.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 仓库管理工具集 — Prompt-Based Function Calling
 *
 * DashScope 不原生支持 OpenAI Function Calling，改用 Prompt 描述工具 + JSON 返回的方式。
 * LLM 被告知所有可用工具及其参数，需要时返回 JSON 格式的调用指令，
 * AIService 解析 JSON 后通过 dispatch() 执行对应方法。
 *
 * 工具分类：
 * - 库存查询：queryStockByPart, queryLowStock
 * - 页面导航：navigateToPage
 * - 统计分析：getStatistics, queryPartCount, queryCategoryCount, queryPendingApproval
 * - 知识检索：searchKnowledgeBase
 */
@Component
public class WarehouseTools {

    @Autowired private StockService stockService;
    @Autowired private PartService partService;
    @Autowired private InboundService inboundService;
    @Autowired private OutboundService outboundService;
    @Autowired private PartCategoryService categoryService;
    @Autowired private RAGService ragService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 所有可用工具的定义（会嵌入 System Prompt）
     */
    public static final String TOOL_DEFINITIONS = """
        【可用工具列表】
        你可以调用以下工具获取数据。如果需要调用工具，只返回JSON，不要任何其他文字：
        {"tool":"工具名","params":{"参数名":"值"}}

        如果不需要调用工具（如问候、闲聊），直接返回文本回复。

        工具说明：
        1. queryStockByPart — 查询配件库存
           参数：partName（配件名称）
           示例：{"tool":"queryStockByPart","params":{"partName":"前车门"}}

        2. queryLowStock — 查询库存不足的配件列表
           参数：无
           示例：{"tool":"queryLowStock","params":{}}

        3. navigateToPage — 导航到功能页面
           参数：pageName（页面名称）
           示例：{"tool":"navigateToPage","params":{"pageName":"入库管理"}}

        4. getStatistics — 查询统计数据
           参数：statType（today_inbound/today_outbound/month_inbound/month_outbound/total_stock）
           示例：{"tool":"getStatistics","params":{"statType":"today_inbound"}}

        5. queryPartCount — 查询配件种类数
           参数：无
           示例：{"tool":"queryPartCount","params":{}}

        6. queryCategoryCount — 查询分类数量
           参数：无
           示例：{"tool":"queryCategoryCount","params":{}}

        7. queryPendingApproval — 查询待审批出库单数量
           参数：无
           示例：{"tool":"queryPendingApproval","params":{}}

        8. searchKnowledgeBase — 搜索知识库获取操作指南
           参数：query（要搜索的问题）
           示例：{"tool":"searchKnowledgeBase","params":{"query":"怎么新增供应商"}}

        注意：partName 应该是纯粹的配件名称，不要包含"查"、"还有多少"等查询词。
        例如用户说"火花塞还有多少"，partName 应该是"火花塞"。
        """;

    /**
     * 工具调度器 — 根据 JSON 中的 tool 名称执行对应方法
     */
    public String dispatch(String toolName, Map<String, String> params) {
        return switch (toolName) {
            case "queryStockByPart"    -> queryStockByPart(params.getOrDefault("partName", ""));
            case "queryLowStock"       -> queryLowStock();
            case "navigateToPage"      -> navigateToPage(params.getOrDefault("pageName", ""));
            case "getStatistics"       -> getStatistics(params.getOrDefault("statType", ""));
            case "queryPartCount"      -> queryPartCount();
            case "queryCategoryCount"  -> queryCategoryCount();
            case "queryPendingApproval" -> queryPendingApproval();
            case "searchKnowledgeBase" -> searchKnowledgeBase(params.getOrDefault("query", ""));
            default -> "未知工具: " + toolName;
        };
    }

    /**
     * 从 LLM 响应文本中提取工具调用 JSON，如果没有则返回 null
     */
    public ToolCall extractToolCall(String llmResponse) {
        if (llmResponse == null || llmResponse.isEmpty()) return null;
        try {
            String cleaned = llmResponse.trim();
            // 清理 markdown 代码块
            if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7);
            else if (cleaned.startsWith("```")) cleaned = cleaned.substring(3);
            if (cleaned.endsWith("```")) cleaned = cleaned.substring(0, cleaned.length() - 3);
            cleaned = cleaned.trim();

            // 尝试找 JSON 对象
            int braceStart = cleaned.indexOf('{');
            if (braceStart < 0) return null;
            int braceEnd = cleaned.lastIndexOf('}');
            if (braceEnd <= braceStart) return null;
            String jsonStr = cleaned.substring(braceStart, braceEnd + 1);

            JsonNode root = mapper.readTree(jsonStr);
            if (root.has("tool") && root.has("params")) {
                String toolName = root.get("tool").asText();
                Map<String, String> params = new HashMap<>();
                JsonNode paramsNode = root.get("params");
                if (paramsNode.isObject()) {
                    paramsNode.fields().forEachRemaining(e ->
                        params.put(e.getKey(), e.getValue().asText()));
                }
                return new ToolCall(toolName, params);
            }
        } catch (Exception e) {
            System.err.println("[ToolParse] 解析工具调用失败: " + e.getMessage());
        }
        return null;
    }

    // ==================== 工具实现 ====================

    public String queryStockByPart(String partName) {
        List<Part> parts = partService.listByName(partName);
        if (parts.isEmpty()) {
            return "未找到名为「" + partName + "」的配件信息。请确认配件名称是否正确。";
        }
        StringBuilder sb = new StringBuilder();
        for (Part part : parts) {
            Stock stock = stockService.getByPartId(part.getId());
            if (stock != null) {
                sb.append(String.format("【%s】当前库存：%d %s，安全库存：%d %s\n",
                    part.getPartName(), stock.getQuantity(), part.getUnit(),
                    stock.getSafeStock(), part.getUnit()));
                if (stock.getQuantity() <= stock.getSafeStock()) {
                    sb.append("  ⚠️ 库存低于安全库存，请及时补充！\n");
                }
            } else {
                sb.append(String.format("【%s】暂无库存记录\n", part.getPartName()));
            }
        }
        return sb.toString().trim();
    }

    public String queryLowStock() {
        List<Stock> lowStockList = stockService.getLowStockList();
        if (lowStockList.isEmpty()) {
            return "🎉 目前所有配件库存均充足，没有低于安全库存的配件。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("以下配件库存低于安全库存：\n\n");
        for (Stock stock : lowStockList) {
            Part part = partService.getById(stock.getPartId());
            if (part != null) {
                sb.append(String.format("• %s：当前 %d %s，安全库存 %d %s\n",
                    part.getPartName(), stock.getQuantity(), part.getUnit(),
                    stock.getSafeStock(), part.getUnit()));
            }
        }
        return sb.toString().trim();
    }

    public String navigateToPage(String pageName) {
        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("配件", "/part/list");
        routes.put("库存", "/stock/list");
        routes.put("入库", "/inbound/list");
        routes.put("出库", "/outbound/list");
        routes.put("盘点", "/stock-check/list");
        routes.put("供应商", "/supplier/list");
        routes.put("客户", "/customer/list");
        routes.put("用户", "/user/list");
        routes.put("个人", "/user/profile");
        for (Map.Entry<String, String> e : routes.entrySet()) {
            if (pageName.contains(e.getKey())) {
                return "NAVIGATE:" + e.getValue() + "|正在为您打开「" + e.getKey() + "」页面...";
            }
        }
        return "抱歉，未找到与「" + pageName + "」对应的页面。可用的页面：配件管理、库存管理、入库管理、出库管理、盘点管理、供应商管理、客户管理、用户管理、个人信息";
    }

    public String getStatistics(String statType) {
        return switch (statType) {
            case "today_inbound" -> "📦 今天入库单数量：" + inboundService.getTodayCount() + " 单";
            case "today_outbound" -> "📤 今天出库单数量：" + outboundService.getTodayCount() + " 单";
            case "month_inbound" -> {
                String m = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                yield "📦 本月（" + m + "）入库总数量：" + inboundService.getMonthInboundQty(m) + " 件";
            }
            case "month_outbound" -> {
                String m = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                Map<String, Object> top = outboundService.getTopOutboundPart(m);
                yield top != null
                    ? "🏆 本月（" + m + "）出库最多的配件：【" + top.get("partName") + "】，出库数量：" + top.get("quantity") + " 件"
                    : "本月暂无出库记录";
            }
            case "total_stock" -> "📊 当前库存总数量：" + stockService.getTotalStock() + " 件";
            default -> "不支持的统计类型：" + statType;
        };
    }

    public String queryPartCount()     { return "📦 系统中共有 " + partService.count() + " 种配件"; }
    public String queryCategoryCount() { return "📁 系统中共有 " + categoryService.count() + " 个配件分类"; }
    public String queryPendingApproval() { return "📋 目前有 " + outboundService.countPendingApproval() + " 个出库单需要审批"; }

    public String searchKnowledgeBase(String query) {
        try {
            String result = ragService.query(query);
            return result != null ? result : "知识库中暂时没有找到关于「" + query + "」的相关内容。";
        } catch (Exception e) {
            return "搜索知识库时出现错误：" + e.getMessage();
        }
    }

    // ==================== 内部类 ====================

    public record ToolCall(String toolName, Map<String, String> params) {}
}
