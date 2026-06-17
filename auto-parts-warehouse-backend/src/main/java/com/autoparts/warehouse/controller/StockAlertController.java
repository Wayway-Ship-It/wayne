package com.autoparts.warehouse.controller;

import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.service.StockAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-alert")
public class StockAlertController {

    @Autowired
    private StockAlertService stockAlertService;

    /**
     * 手动触发库存预警检查
     */
    @GetMapping("/check")
    public Result<String> manualCheck() {
        try {
            stockAlertService.manualCheckAndAlert();
            return Result.success("库存预警检查已执行");
        } catch (Exception e) {
            return Result.error("库存预警检查失败: " + e.getMessage());
        }
    }
}
