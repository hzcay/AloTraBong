package com.example.Alotrabong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public String userDashboard() {
        return "user/dashboard";
    }
}