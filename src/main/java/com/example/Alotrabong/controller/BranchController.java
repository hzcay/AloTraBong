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
        return "redirect:/branch-manager/dashboard";
    }

    @GetMapping("/orders")
    public String branchOrders(Model model) {
        return "redirect:/branch-manager/orders";
    }

    @GetMapping("/menu")
    public String branchMenu(Model model) {
        return "redirect:/branch-manager/menu";
    }

    @GetMapping("/staff")
    public String branchStaff(Model model) {
        return "redirect:/branch-manager/shippers";
    }
}