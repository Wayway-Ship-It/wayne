package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.Customer;
import com.autoparts.warehouse.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    @PostMapping("/save")
    public Result save(@RequestBody Customer customer) {
        try {
            customerService.saveOrUpdate(customer);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            customerService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public Result<IPage<Customer>> list(@RequestParam Map<String, Object> params) {
        try {
            int page = Integer.parseInt(params.getOrDefault("page", "1").toString());
            int size = Integer.parseInt(params.getOrDefault("size", "10").toString());
            String name = params.getOrDefault("name", "").toString();
            String contactPerson = params.getOrDefault("contactPerson", "").toString();
            
            Page<Customer> pageInfo = new Page<>(page, size);
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Customer> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            if (!name.isEmpty()) {
                wrapper.like("name", name);
            }
            if (!contactPerson.isEmpty()) {
                wrapper.like("contact_person", contactPerson);
            }
            IPage<Customer> result = customerService.page(pageInfo, wrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/get/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        try {
            Customer customer = customerService.getById(id);
            return Result.success(customer);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}