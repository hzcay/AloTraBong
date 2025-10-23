package com.example.Alotrabong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/auth"; // vào / tự nhảy qua /auth
    }

    @GetMapping("/auth")
    public String loginPage() {
        // trả về templates/auth/login.html
        return "auth/login_and_register";
    }
}
