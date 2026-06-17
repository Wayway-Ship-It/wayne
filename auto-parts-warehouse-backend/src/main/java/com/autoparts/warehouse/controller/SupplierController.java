package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Supplier;
import com.autoparts.warehouse.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;
    
    @PostMapping("/save")
    public Result save(@RequestBody Supplier supplier) {
        try {
            supplierService.saveOrUpdate(supplier);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            supplierService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public Result<IPage<Supplier>> list(@RequestParam Map<String, Object> params) {
        try {
            int page = Integer.parseInt(params.getOrDefault("page", "1").toString());
            int size = Integer.parseInt(params.getOrDefault("size", "10").toString());
            String name = params.getOrDefault("name", "").toString();
            String contactPerson = params.getOrDefault("contactPerson", "").toString();
            
            Page<Supplier> pageInfo = new Page<>(page, size);
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Supplier> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            if (!name.isEmpty()) {
                wrapper.like("name", name);
            }
            if (!contactPerson.isEmpty()) {
                wrapper.like("contact_person", contactPerson);
            }
            IPage<Supplier> result = supplierService.page(pageInfo, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/get/{id}")
    public Result<Supplier> get(@PathVariable Long id) {
        try {
            Supplier supplier = supplierService.getById(id);
            return Result.success(supplier);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}