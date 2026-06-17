package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Outbound;
import com.autoparts.warehouse.service.OutboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/outbound")
public class OutboundController {
    @Autowired
    private OutboundService outboundService;

    @GetMapping("/list")
    public Result<Page<Outbound>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        return Result.success(outboundService.pageQuery(pageNum, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Outbound> getById(@PathVariable Long id) {
        return Result.success(outboundService.getById(id));
    }

    @PostMapping("/create")
    public Result<Outbound> create(@RequestBody Outbound outbound) {
        try {
            return Result.success(outboundService.createOutbound(outbound));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public Result<Outbound> approve(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Integer approveStatus = Integer.valueOf(params.get("approveStatus").toString());
            String approvalRemark = params.get("approvalRemark") != null ? params.get("approvalRemark").toString() : "";
            String approver = params.get("approver") != null ? params.get("approver").toString() : "";
            return Result.success(outboundService.approveOutbound(id, approveStatus, approvalRemark, approver));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        try {
            return Result.success(outboundService.deleteOutbound(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/today")
    public Result<Long> getTodayCount() {
        return Result.success(outboundService.getTodayCount());
    }
}
