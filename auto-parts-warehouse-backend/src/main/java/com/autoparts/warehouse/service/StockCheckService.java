package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.entity.Stock;
import com.autoparts.warehouse.entity.StockCheck;
import com.autoparts.warehouse.mapper.StockCheckMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

@Service
public class StockCheckService extends ServiceImpl<StockCheckMapper, StockCheck> {
    @Autowired
    private StockService stockService;

    @Autowired
    private PartService partService;
    // 1：盘点单分页条件查询
    public Page<StockCheck> pageQuery(Integer pageNum, Integer pageSize, String keyword) {
        Page<StockCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        // 关键词模糊匹配：盘点单号、配件名称、配件编码
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(StockCheck::getCheckNo, keyword)
                    .or().like(StockCheck::getPartName, keyword)
                    .or().like(StockCheck::getPartCode, keyword));
        }
        // 按创建时间倒序
        wrapper.orderByDesc(StockCheck::getCreateTime);
        return this.page(page, wrapper);
    }
    // 2：创建盘点单
    public StockCheck createCheck(StockCheck stockCheck) {
        Part part = partService.getById(stockCheck.getPartId());
        if (part == null) {
            throw new RuntimeException("配件不存在");
        }
        // 获取系统账面库存，自动填充到盘点单
        Stock stock = stockService.getByPartId(stockCheck.getPartId());
        if (stock != null) {
            stockCheck.setSystemQuantity(stock.getQuantity());
            stockCheck.setCheckLocation(stock.getWarehouseLocation());
        } else {
            stockCheck.setSystemQuantity(0); // 设置默认值
        }
        // 生成盘点单号：CHECK + 时间戳 + 配件编码
        String checkNo = "CHECK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + part.getPartCode();
        stockCheck.setCheckNo(checkNo);
        stockCheck.setPartCode(part.getPartCode());
        stockCheck.setPartName(part.getPartName());
        stockCheck.setCategory(part.getCategory());
        stockCheck.setStatus(0);

        this.save(stockCheck);
        return stockCheck;
    }

    // 3：批量创建定期盘点单（所有配件一次性清点）
    // 定期盘点：创建一个汇总的盘点单，包含所有配件的汇总信息
    public StockCheck batchCreateRegularCheck(String checker, String remark, String period) {
        List<Part> allParts = partService.list();
        if (allParts == null || allParts.isEmpty()) {
            throw new RuntimeException("没有可盘点的配件");
        }

        // 统计所有配件的总数量和系统库存
        int totalSystemQuantity = 0;
        StringBuilder partsSummaryBuilder = new StringBuilder();
        
        // 按分类统计
        java.util.Map<String, Integer> categorySystemQuantityMap = new java.util.HashMap<>();
        java.util.Map<String, java.util.List<String>> categoryPartsMap = new java.util.HashMap<>();
        
        for (int i = 0; i < allParts.size(); i++) {
            Part part = allParts.get(i);
            Stock stock = stockService.getByPartId(part.getId());
            int systemQty = stock != null ? stock.getQuantity() : 0;
            totalSystemQuantity += systemQty;
            
            // 按分类统计
            String category = part.getCategory();
            categorySystemQuantityMap.put(category, categorySystemQuantityMap.getOrDefault(category, 0) + systemQty);
            
            if (!categoryPartsMap.containsKey(category)) {
                categoryPartsMap.put(category, new java.util.ArrayList<>());
            }
            categoryPartsMap.get(category).add(String.format("%s(%s): 账面%d", 
                part.getPartName(), part.getPartCode(), systemQty));
        }
        
        // 构建分类汇总信息
        partsSummaryBuilder.append("【分类汇总】\n");
        for (java.util.Map.Entry<String, Integer> entry : categorySystemQuantityMap.entrySet()) {
            String category = entry.getKey();
            partsSummaryBuilder.append(String.format("%s: 总账面%d\n", category, entry.getValue()));
            java.util.List<String> parts = categoryPartsMap.get(category);
            for (String partInfo : parts) {
                partsSummaryBuilder.append(String.format("%s: %s\n", category, partInfo));
            }
            partsSummaryBuilder.append("\n"); // 不同分类另起一行
        }
        
        partsSummaryBuilder.append("【总计】\n");
        partsSummaryBuilder.append("总账面: " + totalSystemQuantity);

        // 创建一个汇总的盘点单
        StockCheck stockCheck = new StockCheck();
        String periodLabel = "";
        if ("monthly".equals(period)) {
            periodLabel = "月度";
        } else if ("quarterly".equals(period)) {
            periodLabel = "季度";
        } else if ("yearly".equals(period)) {
            periodLabel = "年度";
        }
        
        // 生成盘点单号：CHECK + 时间戳 + 周期类型
        String checkNo = periodLabel + "CHECK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        stockCheck.setCheckNo(checkNo);
        stockCheck.setPartName("所有配件汇总");
        stockCheck.setCategory("汇总");
        stockCheck.setPeriod(period);
        stockCheck.setSystemQuantity(totalSystemQuantity);
        stockCheck.setChecker(checker);
        stockCheck.setRemark(remark);
        stockCheck.setStatus(0);
        stockCheck.setPartsSummary(partsSummaryBuilder.toString());

        this.save(stockCheck);
        return stockCheck;
    }
    // 4：完成盘点
    @Transactional(rollbackFor = Exception.class)
    public StockCheck completeCheck(Long id, Integer actualQuantity, String remark) {
        StockCheck stockCheck = this.getById(id);
        if (stockCheck == null) {
            throw new RuntimeException("盘点单不存在");
        }
        if (stockCheck.getStatus() == 1) {
            throw new RuntimeException("盘点单已完成");
        }

        stockCheck.setActualQuantity(actualQuantity);

        // 填写实际差异、计算差异值
        Integer systemQuantity = stockCheck.getSystemQuantity() != null ? stockCheck.getSystemQuantity() : 0;
        stockCheck.setDifference(actualQuantity - systemQuantity);
        stockCheck.setRemark(remark);

        // 如果有差异，需要管理员确认
        if (stockCheck.getDifference() != 0) {
            stockCheck.setStatus(0); // 保持盘点中状态
            stockCheck.setConfirmStatus(0); // 待确认
        } else {
            // 无差异，直接完成
            stockCheck.setStatus(1);
            stockCheck.setConfirmStatus(1); // 无需确认
        }

        this.updateById(stockCheck);
        return stockCheck;
    }
    // 5：确认盘点
    @Transactional(rollbackFor = Exception.class)
    public StockCheck confirmCheck(Long id, String confirmUser) {
        StockCheck stockCheck = this.getById(id);
        if (stockCheck == null) {
            throw new RuntimeException("盘点单不存在");
        }
        if (stockCheck.getStatus() == 1) {
            throw new RuntimeException("盘点单已完成");
        }
        if (stockCheck.getConfirmStatus() == 1) {
            throw new RuntimeException("盘点单已确认");
        }
        if (stockCheck.getDifference() == 0) {
            throw new RuntimeException("无差异，无需确认");
        }

        // 更新库存
        Stock stock = stockService.getByPartId(stockCheck.getPartId());
        if (stock != null) {
            stock.setQuantity(stockCheck.getActualQuantity());
            stockService.updateById(stock);
        }

        // 更新盘点单状态
        stockCheck.setStatus(1);
        stockCheck.setConfirmStatus(1);
        stockCheck.setConfirmUser(confirmUser);
        stockCheck.setConfirmTime(LocalDateTime.now());

        this.updateById(stockCheck);
        return stockCheck;
    }
}
