package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Part;
import com.autoparts.warehouse.service.PartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/part")
public class PartController {
    @Autowired
    private PartService partService;

    @GetMapping("/list")
    public Result<Page<Part>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String category) {
        return Result.success(partService.pageQuery(pageNum, pageSize, keyword, category));
    }

    @GetMapping("/{id}")
    public Result<Part> getById(@PathVariable Long id) {
        return Result.success(partService.getById(id));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Part part) {
        try {
            return Result.success(partService.addPart(part));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Part part) {
        return Result.success(partService.updatePart(part));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(partService.deletePart(id));
    }

    @GetMapping("/count")
    public Result<Long> getCount() {
        return Result.success(partService.getPartCount());
    }
}
