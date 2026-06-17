package com.autoparts.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class PasswordGeneratorTest {

    @Test
    public void generatePasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String adminPassword = "admin123";
        String userPassword = "user123";
        
        String adminHash = encoder.encode(adminPassword);
        String userHash = encoder.encode(userPassword);
        
        System.out.println("========================================");
        System.out.println("管理员密码 (admin123): " + adminHash);
        System.out.println("用户密码 (user123): " + userHash);
        System.out.println("========================================");
        
        System.out.println("\n验证:");
        System.out.println("admin123 验证: " + encoder.matches(adminPassword, adminHash));
        System.out.println("user123 验证: " + encoder.matches(userPassword, userHash));
    }
}