package com.autoparts.warehouse.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin123";
        String userPassword = "user123";
        
        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);
        
        System.out.println("管理员密码 (admin123): " + adminHash);
        System.out.println("用户密码 (user123): " + userHash);
        
        System.out.println("\n验证:");
        System.out.println("admin123 验证: " + encoder.matches(adminPassword, adminHash));
        System.out.println("user123 验证: " + encoder.matches(userPassword, userHash));
    }
}