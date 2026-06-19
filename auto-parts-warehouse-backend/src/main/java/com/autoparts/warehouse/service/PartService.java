package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.entity.Stock;
import com.autoparts.warehouse.mapper.PartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PartService extends ServiceImpl<PartMapper, Part> {
    @Autowired
    private StockService stockService;
    
    // 1：配件多条件分页查询（名称/编码/品牌+分类筛选）
    public Page<Part> pageQuery(Integer pageNum, Integer pageSize, String keyword, String category) {
        Page<Part> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        // 搜索关键词：匹配配件名、配件编码、品牌
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Part::getPartName, keyword)
                    .or().like(Part::getPartCode, keyword)
                    .or().like(Part::getBrand, keyword));
        }
        // 按配件分类精确筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Part::getCategory, category);
        }
        // 按配件编码排序并返回分页数据
        wrapper.orderByAsc(Part::getPartCode);
        return this.page(page, wrapper);
    }

    // 2：新增配件（支持恢复已删除的配件）
    public boolean addPart(Part part) {
        try {
            // 先尝试删除已删除的相同编码记录
            baseMapper.deleteDeletedByPartCode(part.getPartCode());
            // 再保存新配件
            part.setStatus(1);
            boolean saved = this.save(part);
            
            // 同步创建库存记录
            if (saved) {
                Stock stock = new Stock();
                stock.setPartId(part.getId());
                stock.setPartCode(part.getPartCode());
                stock.setPartName(part.getPartName());
                stock.setQuantity(0); // 当前库存默认为0
                stock.setSafeStock(5); // 安全库存默认为5
                stockService.save(stock);
            }
            
            return saved;
        } catch (Exception e) {
            // 如果仍然冲突，说明有未删除的记录存在
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && e.getMessage().contains("part_code")) {
                throw new RuntimeException("配件编码已存在");
            }
            // 其他错误直接抛出
            throw e;
        }
    }

    public boolean updatePart(Part part) {
        return this.updateById(part);
    }

    public boolean deletePart(Long id) {
        // 先删除对应的库存记录
        LambdaQueryWrapper<Stock> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.eq(Stock::getPartId, id);
        stockService.remove(stockWrapper);
        
        // 再删除配件
        return this.removeById(id);
    }

    public long getPartCount() {
        return this.count();
    }

    // AI助手：按名称模糊查询配件列表
    public java.util.List<Part> listByName(String name) {
        LambdaQueryWrapper<Part> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Part::getPartName, name);
        wrapper.eq(Part::getStatus, 1);
        return this.list(wrapper);
    }
}
