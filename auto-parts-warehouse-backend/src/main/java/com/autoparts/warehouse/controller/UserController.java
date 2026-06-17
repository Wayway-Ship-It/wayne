package com.autoparts.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.entity.User;
import com.autoparts.warehouse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword));
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody User user) {
        try {
            return Result.success(userService.register(user));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody User user) {
        return Result.success(userService.updateUserInfo(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    @PutMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String oldPassword = params.get("oldPassword").toString();
            String newPassword = params.get("newPassword").toString();
            boolean result = userService.changePassword(id, oldPassword, newPassword);
            System.out.println("密码修改结果: " + result);
            return Result.success(result);
        } catch (Exception e) {
            System.out.println("密码修改异常: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
