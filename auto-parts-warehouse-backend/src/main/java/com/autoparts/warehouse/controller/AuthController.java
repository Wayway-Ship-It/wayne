package com.autoparts.warehouse.controller;

import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.dto.LoginRequest;
import com.autoparts.warehouse.dto.LoginResponse;
import com.autoparts.warehouse.entity.User;
import com.autoparts.warehouse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody User user) {
        try {
            boolean result = userService.register(user);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
