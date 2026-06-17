package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Inbound;
import com.autoparts.warehouse.service.InboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inbound")
public class InboundController {
    @Autowired
    private InboundService inboundService;

    @GetMapping("/list")
    public Result<Page<Inbound>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword) {
        return Result.success(inboundService.pageQuery(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public Result<Inbound> getById(@PathVariable Long id) {
        return Result.success(inboundService.getById(id));
    }

    @PostMapping("/create")
    public Result<Inbound> create(@RequestBody Inbound inbound) {
        try {
            return Result.success(inboundService.createInbound(inbound));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(inboundService.deleteInbound(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/today")
    public Result<Long> getTodayCount() {
        return Result.success(inboundService.getTodayCount());
    }
}
