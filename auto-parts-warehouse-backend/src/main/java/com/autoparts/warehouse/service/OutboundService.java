package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.entity.Customer;
import com.autoparts.warehouse.entity.Outbound;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.entity.Stock;
import com.autoparts.warehouse.mapper.OutboundMapper;
import com.autoparts.warehouse.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OutboundService extends ServiceImpl<OutboundMapper, Outbound> {
    @Autowired
    private StockService stockService;

    @Autowired
    private PartService partService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private EmailService emailService;

    // 管理员邮箱列表
    private static final String[] ADMIN_EMAILS = {"2060045841@qq.com"};
    // 1：出库单分页条件查询
    public Page<Outbound> pageQuery(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<Outbound> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        // 关键词模糊匹配：单号、配件名称、配件编码
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Outbound::getOrderNo, keyword)
                    .or().like(Outbound::getPartName, keyword)
                    .or().like(Outbound::getPartCode, keyword));
        }

        if (status != null) {
            wrapper.eq(Outbound::getStatus, status);
        }

        wrapper.orderByDesc(Outbound::getCreateTime);
        return this.page(page, wrapper);
    }
    // 2：创建出库单
    public Outbound createOutbound(Outbound outbound) {
        Part part = partService.getById(outbound.getPartId());
        if (part == null) {
            throw new RuntimeException("配件不存在");
        }
        // 校验库存是否充足
        Stock stock = stockService.getByPartId(outbound.getPartId());
        if (stock == null || stock.getQuantity() < outbound.getQuantity()) {
            throw new RuntimeException("库存不足");
        }

        // 生成单号：前缀 + 年月日时分秒 + 4位随机数
        String typePrefix = outbound.getType() != null ? outbound.getType() : "XSCK";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = (int) (Math.random() * 10000);
        String orderNo = typePrefix + dateStr + String.format("%04d", randomNum);
        outbound.setOrderNo(orderNo);
        outbound.setPartCode(part.getPartCode());
        outbound.setPartName(part.getPartName());
        outbound.setStatus(0);
        // 自动计算单价、总价
        if (part.getPrice() != null) {
            outbound.setUnitPrice(part.getPrice());
            outbound.setTotalPrice(part.getPrice().multiply(java.math.BigDecimal.valueOf(outbound.getQuantity())));
        }
        // 根据客户ID设置收货人信息
        if (outbound.getCustomerId() != null) {
            Customer customer = customerService.getById(outbound.getCustomerId());
            if (customer != null) {
                outbound.setReceiver(customer.getContactPerson());
                outbound.setReceiverPhone(customer.getPhone());
                outbound.setReceiverAddress(customer.getAddress());
            }
        }

        this.save(outbound);
        return outbound;
    }

    @Transactional(rollbackFor = Exception.class)
    public Outbound approveOutbound(Long id, Integer approveStatus, String approvalRemark, String approver) {
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new RuntimeException("出库单不存在");
        }
        if (outbound.getStatus() != 0) {
            throw new RuntimeException("出库单已审批");
        }

        outbound.setStatus(approveStatus);
        outbound.setApprover(approver);
        outbound.setApprovalRemark(approvalRemark);
        outbound.setApprovalTime(LocalDateTime.now());

        if (approveStatus == 1) {
            // 再次检查库存是否充足
            Stock stock = stockService.getByPartId(outbound.getPartId());
            if (stock == null || stock.getQuantity() < outbound.getQuantity()) {
                throw new RuntimeException("库存不足");
            }
            
            // 记录出库前的库存
            int currentStock = stock.getQuantity();
            int safeStock = stock.getSafeStock() != null ? stock.getSafeStock() : 0;
            
            // 执行出库操作
            stockService.decreaseStock(outbound.getPartId(), outbound.getQuantity());
            outbound.setStatus(2);
            
            // 检查出库后库存是否低于安全库存，如果是则发送预警邮件
            int remainingStock = currentStock - outbound.getQuantity();
            if (safeStock > 0 && remainingStock <= safeStock) {
                for (String email : ADMIN_EMAILS) {
                    emailService.sendOutboundAlertEmail(
                        email,
                        outbound.getPartName(),
                        currentStock,
                        outbound.getQuantity(),
                        safeStock
                    );
                }
            }
        }

        this.updateById(outbound);
        return outbound;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOutbound(Long id) {
        Outbound outbound = this.getById(id);
        if (outbound == null) {
            throw new RuntimeException("出库单不存在");
        }

        return this.removeById(id);
    }

    public long getTodayCount() {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Outbound::getCreateTime, java.time.LocalDate.now().atStartOfDay());
        return this.count(wrapper);
    }

    // AI助手：统计待审批的出库单数量
    public long countPendingApproval() {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Outbound::getStatus, 1); // 1表示待审批状态
        return this.count(wrapper);
    }

    // AI助手：获取本月销售出库最多的配件
    public java.util.Map<String, Object> getTopOutboundPart(String month) {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Outbound::getCreateTime, month);
        wrapper.eq(Outbound::getStatus, 2); // 已完成的出库单
        wrapper.eq(Outbound::getType, "XSCK"); // 销售出库
        
        java.util.List<Outbound> list = this.list(wrapper);
        
        // 按配件名称分组统计出库数量
        java.util.Map<String, Integer> partCount = new java.util.HashMap<>();
        for (Outbound outbound : list) {
            String partName = outbound.getPartName();
            partCount.put(partName, partCount.getOrDefault(partName, 0) + outbound.getQuantity());
        }
        
        // 找出数量最多的配件
        String topPart = null;
        int maxQty = 0;
        for (java.util.Map.Entry<String, Integer> entry : partCount.entrySet()) {
            if (entry.getValue() > maxQty) {
                maxQty = entry.getValue();
                topPart = entry.getKey();
            }
        }
        
        if (topPart != null) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("partName", topPart);
            result.put("quantity", maxQty);
            return result;
        }
        
        return null;
    }
}
