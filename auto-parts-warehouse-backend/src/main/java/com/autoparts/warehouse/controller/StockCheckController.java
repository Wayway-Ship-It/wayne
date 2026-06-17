package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.StockCheck;
import com.autoparts.warehouse.service.StockCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-check")
public class StockCheckController {
    @Autowired
    private StockCheckService stockCheckService;

    @GetMapping("/list")
    public Result<Page<StockCheck>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(stockCheckService.pageQuery(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<StockCheck> getById(@PathVariable Long id) {
        return Result.success(stockCheckService.getById(id));
    }

    @PostMapping("/create")
    public Result<StockCheck> create(@RequestBody StockCheck stockCheck) {
        try {
            return Result.success(stockCheckService.createCheck(stockCheck));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/batch-create")
    public Result<StockCheck> batchCreate(@RequestBody Map<String, String> params) {
        try {
            String checker = params.get("checker");
            String remark = params.get("remark");
            String period = params.get("period");
            return Result.success(stockCheckService.batchCreateRegularCheck(checker, remark, period));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/complete")
    public Result<StockCheck> complete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer actualQuantity = Integer.valueOf(params.get("actualQuantity").toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : "";
            return Result.success(stockCheckService.completeCheck(id, actualQuantity, remark));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(stockCheckService.removeById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/confirm")
    public Result<StockCheck> confirm(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String confirmUser = params.get("confirmUser").toString();
            return Result.success(stockCheckService.confirmCheck(id, confirmUser));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
