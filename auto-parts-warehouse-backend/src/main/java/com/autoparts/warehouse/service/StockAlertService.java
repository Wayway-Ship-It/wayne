package com.autoparts.warehouse.service;

import com.autoparts.warehouse.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockAlertService {

    @Autowired
    private StockService stockService;

    @Autowired
    private EmailService emailService;

    // 管理员邮箱列表（可以配置到数据库或配置文件中）
    private static final String[] ADMIN_EMAILS = {"2060045841@qq.com"};

    /**
     * 定时检查库存并发送预警邮件
     * 每天上午9点和下午3点执行
     */
    @Scheduled(cron = "0 0 9,15 * * ?")
    public void checkStockAndAlert() {
        System.out.println("开始执行库存预警检查...");
        
        try {
            // 查询所有库存数据
            List<Stock> stockList = stockService.list();
            
            // 筛选出库存不足的商品
            List<Stock> lowStockList = new ArrayList<>();
            for (Stock stock : stockList) {
                if (stock.getQuantity() <= stock.getSafeStock() && stock.getSafeStock() > 0) {
                    lowStockList.add(stock);
                }
            }
            
            // 将所有库存不足的商品汇总到一封邮件发送给管理员
            if (!lowStockList.isEmpty()) {
                for (String email : ADMIN_EMAILS) {
                    emailService.sendBatchStockAlertEmail(email, lowStockList);
                }
            } else {
                System.out.println("没有库存不足的商品");
            }
            
            System.out.println("库存预警检查完成");
        } catch (Exception e) {
            System.err.println("库存预警检查失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发库存检查（可通过API调用）
     */
    public void manualCheckAndAlert() {
        checkStockAndAlert();
    }
}
