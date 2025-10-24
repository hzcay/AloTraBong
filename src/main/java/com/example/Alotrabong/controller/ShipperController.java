package com.example.Alotrabong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipper")
@PreAuthorize("hasAnyRole('ADMIN', 'SHIPPER')")
public class ShipperController {

    @GetMapping("/dashboard")
    public String shipperDashboard(Model model) {
        model.addAttribute("title", "Delivery Dashboard");
        return "shipper/dashboard";
    }

    @GetMapping("/deliveries")
    public String deliveries(Model model) {
        model.addAttribute("title", "My Deliveries");
        return "shipper/deliveries";
    }

    @GetMapping("/history")
    public String deliveryHistory(Model model) {
        model.addAttribute("title", "Delivery History");
        return "shipper/history";
    }
}