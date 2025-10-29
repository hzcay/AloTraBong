package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.*;
import com.example.Alotrabong.dto.AddItemToBranchRequest;
import com.example.Alotrabong.service.BranchManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.Alotrabong.service.BranchAssignmentService;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpSession;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


@Controller
@RequestMapping("/branch-manager")
@PreAuthorize("hasRole('BRANCH_MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class BranchManagerController {

    private final BranchManagerService branchManagerService;
    private final BranchAssignmentService branchAssignmentService;
    private final UserRepository userRepository;
    // Global model attrs for all views under this controller
    @ModelAttribute("currentBranch")
    public BranchDTO injectCurrentBranch(Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        if (branchId == null) return null;
        try {
            return branchManagerService.getBranchInfo(branchId);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== PAGE ROUTES ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication, HttpSession session) {
        // Get from session first to avoid losing assignment between navigations
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) {
                session.setAttribute("branchId", branchId);
            }
        }
        log.info("Branch Manager dashboard for branch: {}", branchId);
        
        // Check if user is assigned to a branch
        if (branchId == null) {
            log.warn("User {} is not assigned to any branch", authentication.getName());
            return "redirect:/branch-manager/no-branch-assigned";
        }
        
        try {
            // Get dashboard data
            BranchDashboardDTO dashboardData = branchManagerService.getDashboardData(branchId);
            model.addAttribute("dashboard", dashboardData);
            model.addAttribute("title", "Branch Dashboard");
            
            return "branch-manager/dashboard";
        } catch (ResourceNotFoundException e) {
            log.error("Branch not found for user {}: {}", authentication.getName(), e.getMessage());
            return "redirect:/branch-manager/no-branch-assigned";
        }
    }

    @GetMapping("/orders")
    public String orders(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager orders for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Quản lý Đơn hàng");
        
        return "branch-manager/orders";
    }

    @GetMapping("/menu")
    public String menu(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager menu for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Quản lý Menu & Kho");
        
        return "branch-manager/menu";
    }

    @GetMapping("/add-items")
    public String addItems(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager add items for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Thêm sản phẩm vào Chi nhánh");
        
        return "branch-manager/add-items";
    }

    @GetMapping("/shippers")
    public String shippers(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager shippers for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Quản lý Shipper");
        
        return "branch-manager/shippers";
    }

    @GetMapping("/promotions")
    public String promotions(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager promotions for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Quản lý Khuyến mãi");
        
        return "branch-manager/promotions";
    }

    @GetMapping("/revenue")
    public String revenue(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager revenue for branch: {}", branchId);
        
        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Thống kê Doanh thu");
        
        return "branch-manager/revenue";
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager settings for branch: {}", branchId);
        
        BranchDTO branchInfo = branchManagerService.getBranchInfo(branchId);
        model.addAttribute("branch", branchInfo);
        model.addAttribute("title", "Cài đặt Chi nhánh");
        
        return "branch-manager/settings";
    }

    @GetMapping("/chat")
    public String chat(Model model, Authentication authentication, HttpSession session) {
        String branchId = (String) session.getAttribute("branchId");
        if (branchId == null) {
            branchId = getBranchIdFromAuth(authentication);
            if (branchId != null) session.setAttribute("branchId", branchId);
        }
        log.info("Branch Manager chat for branch: {}", branchId);

        model.addAttribute("branchId", branchId);
        model.addAttribute("title", "Chat Chi nhánh");
        try {
            if (authentication != null && authentication.getName() != null) {
                User me = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (me != null) {
                    model.addAttribute("currentUserId", me.getUserId());
                }
            }
        } catch (Exception ignored) {}

        return "branch-manager/chat";
    }

    @GetMapping("/no-branch-assigned")
    public String noBranchAssigned(Model model) {
        model.addAttribute("title", "Chưa được phân công chi nhánh");
        return "branch-manager/no-branch-assigned";
    }

    // ==================== API ENDPOINTS ====================

    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<BranchDashboardDTO> getDashboardData(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        BranchDashboardDTO dashboard = branchManagerService.getDashboardData(branchId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<Page<OrderDTO>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDTO> orders = branchManagerService.getOrders(branchId, pageable, status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/api/orders/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getOrderStats(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        Map<String, Long> stats = branchManagerService.getOrderStats(branchId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/orders/{orderId}")
    @ResponseBody
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable String orderId,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        OrderDTO order = branchManagerService.getOrderById(orderId, branchId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/api/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        OrderDTO order = branchManagerService.updateOrderStatus(orderId, status, branchId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/menu")
    @ResponseBody
    public ResponseEntity<List<BranchMenuItemDTO>> getMenuItems(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<BranchMenuItemDTO> menuItems = branchManagerService.getMenuItems(branchId);
        return ResponseEntity.ok(menuItems);
    }

    @GetMapping("/api/menu/low-stock")
    @ResponseBody
    public ResponseEntity<List<BranchMenuItemDTO>> getLowStockItems(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<BranchMenuItemDTO> lowStockItems = branchManagerService.getLowStockItems(branchId);
        return ResponseEntity.ok(lowStockItems);
    }

    @GetMapping("/api/available-items")
    @ResponseBody
    public ResponseEntity<List<ItemDTO>> getAvailableItems(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<ItemDTO> availableItems = branchManagerService.getAvailableItemsNotInBranch(branchId);
        return ResponseEntity.ok(availableItems);
    }

    @PostMapping("/api/menu/add-item")
    @ResponseBody
    public ResponseEntity<BranchMenuItemDTO> addItemToBranch(
            @RequestBody AddItemToBranchRequest request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        BranchMenuItemDTO item = branchManagerService.addItemToBranch(branchId, request);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/api/menu/{itemId}/price")
    @ResponseBody
    public ResponseEntity<BranchMenuItemDTO> updateItemPrice(
            @PathVariable String itemId,
            @RequestBody Map<String, BigDecimal> request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        BigDecimal newPrice = request.get("price");
        BranchMenuItemDTO item = branchManagerService.updateItemPrice(branchId, itemId, newPrice);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/api/menu/{itemId}/availability")
    @ResponseBody
    public ResponseEntity<BranchMenuItemDTO> updateItemAvailability(
            @PathVariable String itemId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        Boolean isAvailable = request.get("isAvailable");
        BranchMenuItemDTO item = branchManagerService.updateItemAvailability(branchId, itemId, isAvailable);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/api/menu/{itemId}/inventory")
    @ResponseBody
    public ResponseEntity<BranchMenuItemDTO> updateInventory(
            @PathVariable String itemId,
            @RequestBody Map<String, Integer> request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        Integer quantity = request.get("quantity");
        BranchMenuItemDTO item = branchManagerService.updateInventory(branchId, itemId, quantity);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/api/shippers")
    @ResponseBody
    public ResponseEntity<List<ShipperDTO>> getShippers(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<ShipperDTO> shippers = branchManagerService.getShippers(branchId);
        return ResponseEntity.ok(shippers);
    }

    @PostMapping("/api/shippers")
    @ResponseBody
    public ResponseEntity<ShipperDTO> createShipper(
            @RequestBody CreateShipperRequest request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        ShipperDTO shipper = branchManagerService.createShipper(branchId, request);
        return ResponseEntity.ok(shipper);
    }

    @GetMapping("/api/shippers/{shipperId}")
    @ResponseBody
    public ResponseEntity<ShipperDTO> getShipperById(
            @PathVariable String shipperId,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        ShipperDTO shipper = branchManagerService.getShipperById(shipperId, branchId);
        return ResponseEntity.ok(shipper);
    }

    @GetMapping("/api/shippers/{shipperId}/stats")
    @ResponseBody
    public ResponseEntity<ShipperStatsDTO> getShipperStats(
            @PathVariable String shipperId,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        ShipperStatsDTO stats = branchManagerService.getShipperStats(shipperId, branchId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/shippers/{shipperId}/shipments")
    @ResponseBody
    public ResponseEntity<List<ShipmentDTO>> getShipperDeliveryHistory(
            @PathVariable String shipperId,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<ShipmentDTO> shipments = branchManagerService.getShipperDeliveryHistory(shipperId, branchId);
        return ResponseEntity.ok(shipments);
    }

    @PutMapping("/api/shippers/{shipperId}/status")
    @ResponseBody
    public ResponseEntity<ShipperDTO> updateShipperStatus(
            @PathVariable String shipperId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        Boolean isActive = request.get("isActive");
        ShipperDTO shipper = branchManagerService.updateShipperStatus(shipperId, isActive, branchId);
        return ResponseEntity.ok(shipper);
    }

    @PostMapping("/api/orders/{orderId}/assign-shipper")
    @ResponseBody
    public ResponseEntity<OrderDTO> assignShipper(
            @PathVariable String orderId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        String shipperId = request.get("shipperId");
        OrderDTO order = branchManagerService.assignShipper(orderId, shipperId, branchId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/promotions")
    @ResponseBody
    public ResponseEntity<List<PromotionDTO>> getPromotions(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        List<PromotionDTO> promotions = branchManagerService.getPromotions(branchId);
        return ResponseEntity.ok(promotions);
    }

    @PostMapping("/api/promotions")
    @ResponseBody
    public ResponseEntity<PromotionDTO> createPromotion(
            @RequestBody CreatePromotionRequest request,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        PromotionDTO promotion = branchManagerService.createPromotion(branchId, request);
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/api/revenue/summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRevenueSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentMethod,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        Map<String, Object> summary = branchManagerService.getRevenueSummary(branchId, start, end, paymentMethod);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/api/revenue/top-items")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTopSellingItems(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        List<Map<String, Object>> topItems = branchManagerService.getTopSellingItems(branchId, start, end, limit);
        return ResponseEntity.ok(topItems);
    }

    @GetMapping("/api/revenue/daily")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        List<Map<String, Object>> dailyRevenue = branchManagerService.getDailyRevenue(branchId, start, end);
        return ResponseEntity.ok(dailyRevenue);
    }

    @GetMapping("/api/debug/orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugOrders(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("branchId", branchId);
        debug.put("totalOrders", branchManagerService.getOrderStats(branchId));
        
        return ResponseEntity.ok(debug);
    }

    @PutMapping("/api/branch")
    @ResponseBody
    public ResponseEntity<BranchDTO> updateBranchInfo(
            @RequestBody BranchDTO branchDTO,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        BranchDTO updatedBranch = branchManagerService.updateBranchInfo(branchId, branchDTO);
        return ResponseEntity.ok(updatedBranch);
    }

    @GetMapping("/api/shipping-rates")
    @ResponseBody
    public ResponseEntity<ShippingRateDTO> getShippingRates(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        ShippingRateDTO rates = branchManagerService.getShippingRates(branchId);
        return ResponseEntity.ok(rates);
    }

    @PutMapping("/api/shipping-rates")
    @ResponseBody
    public ResponseEntity<ShippingRateDTO> updateShippingRates(
            @RequestBody ShippingRateDTO ratesDTO,
            Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        ShippingRateDTO rates = branchManagerService.updateShippingRates(branchId, ratesDTO);
        return ResponseEntity.ok(rates);
    }

    // ==================== HELPER METHODS ====================

    private String getBranchIdFromAuth(Authentication authentication) {
        return branchAssignmentService.getBranchIdFromAuth(authentication);
    }

    private String getBranchIdFromAuthOrThrow(Authentication authentication) {
        String branchId = getBranchIdFromAuth(authentication);
        if (branchId == null) {
            throw new ResourceNotFoundException("User is not assigned to any branch");
        }
        return branchId;
    }
}
