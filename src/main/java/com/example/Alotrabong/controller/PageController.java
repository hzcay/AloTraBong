package com.example.Alotrabong.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "index"; // hiển thị trang chủ
    }

    @GetMapping("/auth")
    public String loginPage() {
        // trả về templates/auth/login.html
        return "auth/login_and_register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
