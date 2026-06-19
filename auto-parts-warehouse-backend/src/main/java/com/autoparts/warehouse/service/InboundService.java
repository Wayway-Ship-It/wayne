package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Inbound;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.entity.Supplier;
import com.autoparts.warehouse.mapper.InboundMapper;
import com.autoparts.warehouse.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InboundService extends ServiceImpl<InboundMapper, Inbound> {
    @Autowired
    private StockService stockService;

    @Autowired
    private PartService partService;

    @Autowired
    private SupplierService supplierService;

    // 1：入库单分页条件查询
    public Page<Inbound> pageQuery(Integer pageNum, Integer pageSize, String keyword) {
        Page<Inbound> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        // 关键词模糊匹配：单号、配件名称、配件编码
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Inbound::getOrderNo, keyword)
                    .or().like(Inbound::getPartName, keyword)
                    .or().like(Inbound::getPartCode, keyword));
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(Inbound::getCreateTime);
        return this.page(page, wrapper);
    }
    // 2：创建入库单
    @Transactional(rollbackFor = Exception.class)
    public Inbound createInbound(Inbound inbound) {
        Part part = partService.getById(inbound.getPartId());
        if (part == null) {
            throw new RuntimeException("配件不存在");
        }
        // 生成单号：前缀 + 年月日时分秒 + 4位随机数
        String typePrefix = inbound.getType() != null ? inbound.getType() : "CGRK";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = (int) (Math.random() * 10000);
        String orderNo = typePrefix + dateStr + String.format("%04d", randomNum);
        inbound.setOrderNo(orderNo);
        inbound.setPartCode(part.getPartCode());
        inbound.setPartName(part.getPartName());
        inbound.setStatus(1);
        // 自动计算总价 = 单价 × 数量
        if (inbound.getUnitPrice() != null && inbound.getQuantity() != null) {
            inbound.setTotalPrice(inbound.getUnitPrice().multiply(java.math.BigDecimal.valueOf(inbound.getQuantity())));
        }
        // 根据供应商ID设置供应商名称
        if (inbound.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(inbound.getSupplierId());
            if (supplier != null) {
                inbound.setSupplier(supplier.getName());
            }
        }

        this.save(inbound);

        stockService.increaseStock(inbound.getPartId(), inbound.getQuantity(), inbound.getBatchNo(), inbound.getWarehouseLocation());

        return inbound;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInbound(Long id) {
        Inbound inbound = this.getById(id);
        if (inbound == null) {
            throw new RuntimeException("入库单不存在");
        }

        return this.removeById(id);
    }

    public long getTodayCount() {
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Inbound::getCreateTime, java.time.LocalDate.now().atStartOfDay());
        return this.count(wrapper);
    }

    // AI助手：获取本月入库总数量
    public long getMonthInboundQty(String month) {
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Inbound::getCreateTime, month);
        wrapper.eq(Inbound::getStatus, 2); // 已完成的入库单
        return this.list(wrapper).stream()
            .mapToLong(Inbound::getQuantity)
            .sum();
    }
}
