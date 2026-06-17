package com.autoparts.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.autoparts.warehouse.dto.LoginRequest;
import com.autoparts.warehouse.dto.LoginResponse;
import com.autoparts.warehouse.entity.User;
import com.autoparts.warehouse.mapper.UserMapper;
import com.autoparts.warehouse.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    @Autowired
    private JwtUtil jwtUtil;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        //1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = this.getOne(wrapper);
        // 2. 判断用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 3. 判断用户是否被禁用
        if (user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }
        // 4. 密码验证
        System.out.println("登录密码: " + request.getPassword());
        System.out.println("数据库密码: " + user.getPassword());
        System.out.println("密码验证结果: " + passwordEncoder.matches(request.getPassword(), user.getPassword()));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        // 5. 登录成功
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    public boolean register(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        return this.save(user);
    }

    public boolean updateUserInfo(User user) {
        return this.updateById(user);
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
}
