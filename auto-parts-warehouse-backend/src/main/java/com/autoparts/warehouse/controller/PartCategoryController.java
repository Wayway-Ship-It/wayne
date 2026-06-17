package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.PartCategory;
import com.autoparts.warehouse.service.PartCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class PartCategoryController {
    @Autowired
    private PartCategoryService partCategoryService;

    @GetMapping("/list")
    public Result<List<PartCategory>> list(@RequestParam(required = false) String keyword) {
        return Result.success(partCategoryService.listAll(keyword));
    }

    @GetMapping("/{id}")
    public Result<PartCategory> getById(@PathVariable Long id) {
        return Result.success(partCategoryService.getById(id));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody PartCategory category) {
        return Result.success(partCategoryService.save(category));
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody PartCategory category) {
        return Result.success(partCategoryService.updateById(category));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(partCategoryService.removeById(id));
    }
}
