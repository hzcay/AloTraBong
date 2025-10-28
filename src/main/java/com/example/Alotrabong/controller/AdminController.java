package com.example.Alotrabong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("title", "User Management");
        return "admin/users";
    }

    @GetMapping("/menu")
    public String menuManagement(Model model) {
        model.addAttribute("title", "Menu Management");
        return "admin/menu";
    }

    @GetMapping("/branches")
    public String branchManagement(Model model) {
        model.addAttribute("title", "Branch Management");
        return "admin/branches";
    }

    @GetMapping("/branch-commissions")
    public String branchCommissions(Model model) {
        model.addAttribute("title", "Branch Commissions Management");
        return "admin/branch-commissions";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("title", "Reports");
        return "admin/reports";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("title", "System Settings");
        return "admin/settings";
    }

    @GetMapping("/promotions")
    public String promotions(Model model) {
        model.addAttribute("title", "Promotion Management");
        return "admin/promotions";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("title", "Shipping Rates Management");
        return "admin/shipping";
    }
}
