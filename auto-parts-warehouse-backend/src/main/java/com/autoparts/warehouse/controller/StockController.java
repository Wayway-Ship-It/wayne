package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Stock;
import com.autoparts.warehouse.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    @Autowired
    private StockService stockService;

    @GetMapping("/list")
    public Result<Page<Stock>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Boolean lowStock) {
        return Result.success(stockService.pageQuery(pageNum, pageSize, keyword, lowStock));
    }

    @GetMapping("/{id}")
    public Result<Stock> getById(@PathVariable Long id) {
        return Result.success(stockService.getById(id));
    }

    @GetMapping("/part/{partId}")
    public Result<Stock> getByPartId(@PathVariable Long partId) {
        return Result.success(stockService.getByPartId(partId));
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Stock stock) {
        return Result.success(stockService.updateById(stock));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(stockService.removeById(id));
    }
}
