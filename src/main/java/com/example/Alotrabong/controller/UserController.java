package com.example.Alotrabong.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    // ------- HOME -------
    @GetMapping({ "", "/" })
    public String userRoot() {
        return "redirect:/user/home";
    }

    @GetMapping({ "/home", "/home/index" })
    public String home() {
        return "user/home/index";
    }

    // ------- PRODUCT -------
    // /user/product/list?cat=...&q=...&page=1
    @GetMapping("/product/list")
    public String productList() {
        return "user/product/list";
    }

    // Nhận cả id hoặc slug (khớp với redirect /p/{slug})
    @GetMapping("/product/detail/{idOrSlug}")
    public String productDetail(@PathVariable("idOrSlug") String idOrSlug) {
        return "user/product/detail";
    }

    // ------- BRANCH -------
    @GetMapping("/branch/list")
    public String branchList() {
        return "user/branch/list";
    }

    // ------- CART -------
    @GetMapping("/cart")
    public String cart() {
        return "user/cart/cart";
    }

    // ------- CHECKOUT -------
    @GetMapping("/checkout")
    public String checkout() {
        return "user/checkout/checkout";
    }

    // ------- ORDER -------
    @GetMapping("/order/history")
    public String orderHistory() {
        return "user/order/history";
    }

    @GetMapping("/order/detail/{code}")
    public String orderDetail(@PathVariable String code) {
        return "user/order/detail";
    }

    // ------- ADDRESS -------
    @GetMapping("/address/manage")
    public String addressManage() {
        return "user/address/manage";
    }

    // ------- FAVORITE -------
    @GetMapping("/favorite/list")
    public String favoriteList() {
        return "user/favorite/list";
    }

    // ------- RECENT -------
    @GetMapping("/recent/list")
    public String recentList() {
        return "user/recent/list";
    }

    // ------- COUPON -------
    @GetMapping("/coupon/list")
    public String couponList() {
        return "user/coupon/list";
    }

    // ------- REVIEW -------
    @GetMapping("/review/write")
    public String reviewWrite(@RequestParam(required = false) String itemId) {
        return "user/review/write";
    }

    // ------- CHAT -------
    @GetMapping("/chat/room/{roomId}")
    public String chatRoom(@PathVariable String roomId) {
        return "user/chat/room";
    }

    // Fallback cũ
    @GetMapping("/dashboard")
    public String dashboardFallback() {
        return "redirect:/user/home";
    }
}