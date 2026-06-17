package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.entity.Stock;
import com.autoparts.warehouse.mapper.PartMapper;
import com.autoparts.warehouse.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StockService extends ServiceImpl<StockMapper, Stock> {
    @Autowired
    private PartMapper partMapper;
    // 1：库存分页条件查询
    public Page<Stock> pageQuery(Integer pageNum, Integer pageSize, String keyword, Boolean lowStock) {
        Page<Stock> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        // 关键词匹配：配件名称、配件编码
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Stock::getPartName, keyword)
                    .or().like(Stock::getPartCode, keyword));
        }
        // 筛选库存不足的配件
        if (lowStock != null && lowStock) {
            wrapper.apply("quantity < safe_stock");
        }
        // 按配件编码排序
        wrapper.orderByAsc(Stock::getPartCode);
        return this.page(page, wrapper);
    }
    // 2：根据配件ID查询库存
    public Stock getByPartId(Long partId) {
        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getPartId, partId);
        wrapper.orderByDesc(Stock::getId);
        return this.getOne(wrapper, false);
    }
    // 3：增加库存
    public void increaseStock(Long partId, Integer quantity, String batchNo, String warehouseLocation) {
        Stock stock = getByPartId(partId);
        // 无库存 → 新建库存记录
        if (stock == null) {
            Part part = partMapper.selectById(partId);
            stock = new Stock();
            stock.setPartId(partId);
            stock.setPartCode(part.getPartCode());
            stock.setPartName(part.getPartName());
            stock.setQuantity(quantity);
            stock.setSafeStock(10);
            stock.setBatchNo(batchNo);
            stock.setWarehouseLocation(warehouseLocation);
            this.save(stock);
        } else {
            // 有库存 → 累加库存数量
            stock.setQuantity(stock.getQuantity() + quantity);
            if (StringUtils.hasText(batchNo)) {
                stock.setBatchNo(batchNo);
            }
            if (StringUtils.hasText(warehouseLocation)) {
                stock.setWarehouseLocation(warehouseLocation);
            }
            this.updateById(stock);
        }
    }
    // 4：减少库存（出库审批通过时调用 → 扣减库存）
    public void decreaseStock(Long partId, Integer quantity) {
        Stock stock = getByPartId(partId);
        if (stock == null) {
            throw new RuntimeException("库存不存在");
        }
        if (stock.getQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        stock.setQuantity(stock.getQuantity() - quantity);
        this.updateById(stock);
    }
}
