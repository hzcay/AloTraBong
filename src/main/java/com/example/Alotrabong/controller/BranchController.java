package com.example.Alotrabong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/branch")
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
public class BranchController {

    @GetMapping("/dashboard")
    public String branchDashboard(Model model) {
        model.addAttribute("title", "Branch Dashboard");
        return "branch/dashboard";
    }

    @GetMapping("/orders")
    public String branchOrders(Model model) {
        model.addAttribute("title", "Branch Orders");
        return "branch/orders";
    }

    @GetMapping("/menu")
    public String branchMenu(Model model) {
        model.addAttribute("title", "Branch Menu");
        return "branch/menu";
    }

    @GetMapping("/staff")
    public String branchStaff(Model model) {
        model.addAttribute("title", "Branch Staff");
        return "branch/staff";
    }
}