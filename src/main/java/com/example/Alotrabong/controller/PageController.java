package com.example.Alotrabong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/user/home";
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

    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/auth?logout";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/auth?logout";
    }

    // ------- URL "đẹp" cho user-facing, redirect sang /user/... -------

    // Menu -> danh sách sản phẩm
    @GetMapping("/menu")
    public String menu() {
        return "redirect:/user/product/list";
    }

    // Ưu đãi -> danh sách coupon
    @GetMapping("/coupons")
    public String coupons() {
        return "redirect:/user/coupon/list";
    }

    @GetMapping({ "/branches", "/branches/list" })
    public String branches() {
        return "redirect:/user/branch/list";
    }

    // Giỏ hàng
    @GetMapping("/cart")
    public String cart() {
        return "redirect:/user/cart";
    }

    // Đơn hàng
    @GetMapping("/orders")
    public String orders() {
        return "redirect:/user/order/history";
    }

    // Địa chỉ
    @GetMapping("/addresses")
    public String addresses() {
        return "redirect:/user/address/manage";
    }

    // Yêu thích
    @GetMapping("/favorites")
    public String favorites() {
        return "redirect:/user/favorite/list";
    }

    // Chi tiết sản phẩm bằng slug "đẹp" -> map vào trang chi tiết ở /user
    @GetMapping("/p/{slug}")
    public String productBySlug() {
        return "redirect:/user/product/detail/{slug}";
    }
}
