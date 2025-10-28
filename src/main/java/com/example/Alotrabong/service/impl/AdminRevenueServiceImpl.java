package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.RevenueReportDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminRevenueServiceImpl implements AdminRevenueService {

    private final OrderRepository orderRepository;
    private final BranchRepository branchRepository;
    private final BranchCommissionRepository branchCommissionRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    @Override
    public Page<RevenueReportDTO> getRevenueByBranch(Pageable pageable, LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting revenue by branch from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        // Get valid orders (DELIVERED status, proper payment status)
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        
        // Group by branch and calculate metrics
        Map<String, List<Order>> ordersByBranch = validOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getBranch().getBranchId()));
        
        List<RevenueReportDTO> reports = new ArrayList<>();
        
        for (Map.Entry<String, List<Order>> entry : ordersByBranch.entrySet()) {
            String currentBranchId = entry.getKey();
            List<Order> branchOrders = entry.getValue();
            
            RevenueReportDTO report = calculateBranchMetrics(currentBranchId, branchOrders, fromDate, toDate);
            reports.add(report);
        }
        
        // Sort by total revenue descending
        reports.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), reports.size());
        List<RevenueReportDTO> pagedReports = reports.subList(start, end);
        
        return new PageImpl<>(pagedReports, pageable, reports.size());
    }

    @Override
    public List<RevenueReportDTO> getRevenueByDate(LocalDate fromDate, LocalDate toDate, String branchId, String period) {
        log.info("Getting revenue by date from {} to {}, branchId: {}, period: {}", fromDate, toDate, branchId, period);
        
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        
        // Group by date
        Map<String, List<Order>> ordersByDate = validOrders.stream()
                .filter(order -> order.getUpdatedAt() != null || order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(order -> {
                    LocalDateTime orderDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
                    if ("monthly".equals(period)) {
                        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    } else {
                        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                }));
        
        List<RevenueReportDTO> reports = new ArrayList<>();
        
        for (Map.Entry<String, List<Order>> entry : ordersByDate.entrySet()) {
            String dateKey = entry.getKey();
            List<Order> dayOrders = entry.getValue();
            
            RevenueReportDTO report = RevenueReportDTO.builder()
                    .reportDate(LocalDate.parse(dateKey))
                    .totalOrders((long) dayOrders.size())
                    .totalRevenue(dayOrders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .avgOrderValue(dayOrders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(dayOrders.size()), 2, java.math.RoundingMode.HALF_UP))
                    .build();
            
            reports.add(report);
        }
        
        // Sort by date
        reports.sort(Comparator.comparing(RevenueReportDTO::getReportDate));
        
        return reports;
    }

    @Override
    public Page<RevenueReportDTO> getRevenueByItem(Pageable pageable, LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting revenue by item from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        List<String> validOrderIds = validOrders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        
        // Get order items for valid orders
        if (validOrderIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        List<OrderItem> orderItems;
        try {
            orderItems = orderItemRepository.findByOrderIdIn(validOrderIds);
        } catch (Exception e) {
            log.error("Error fetching order items: {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Group by item
        Map<String, List<OrderItem>> itemsByItemId = orderItems.stream()
                .filter(oi -> oi.getItem() != null && oi.getItem().getItemId() != null)
                .collect(Collectors.groupingBy(item -> item.getItem().getItemId()));
        
        List<RevenueReportDTO> reports = new ArrayList<>();
        
        for (Map.Entry<String, List<OrderItem>> entry : itemsByItemId.entrySet()) {
            String itemId = entry.getKey();
            List<OrderItem> itemOrders = entry.getValue();
            
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) continue;
            
            // Calculate total quantity and revenue
            long totalQuantity = itemOrders.stream()
                    .filter(oi -> oi.getQuantity() != null)
                    .mapToLong(OrderItem::getQuantity)
                    .sum();
            
            BigDecimal totalRevenue = itemOrders.stream()
                    .filter(oi -> oi.getUnitPrice() != null && oi.getQuantity() != null)
                    .map(oi -> {
                        // Use lineTotal if available, otherwise calculate from unitPrice * quantity
                        if (oi.getLineTotal() != null) {
                            return oi.getLineTotal();
                        } else {
                            return oi.getUnitPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            RevenueReportDTO report = RevenueReportDTO.builder()
                    .itemId(itemId)
                    .itemName(item.getName())
                    .itemQuantity(totalQuantity)
                    .itemRevenue(totalRevenue)
                    .build();
            
            reports.add(report);
        }
        
        // Sort by revenue descending
        reports.sort((a, b) -> b.getItemRevenue().compareTo(a.getItemRevenue()));
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), reports.size());
        List<RevenueReportDTO> pagedReports = reports.subList(start, end);
        
        return new PageImpl<>(pagedReports, pageable, reports.size());
    }

    @Override
    public Map<String, Object> getRevenueSummary(LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting revenue summary from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        
        Map<String, Object> summary = new HashMap<>();
        
        // Basic metrics - filter null amounts
        BigDecimal totalRevenue = validOrders.stream()
                .filter(order -> order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        summary.put("totalOrders", (long) validOrders.size());
        summary.put("totalRevenue", totalRevenue);
        
        // Count delivered orders (validOrders are already DELIVERED status)
        summary.put("deliveredOrders", (long) validOrders.size());
        
        // Calculate system share and branch income
        BigDecimal totalSystemShare = BigDecimal.ZERO;
        BigDecimal totalBranchIncome = BigDecimal.ZERO;
        
        for (Order order : validOrders) {
            try {
                if (order.getTotalAmount() == null) continue;
                
                BranchCommission commission = getCommissionForOrder(order);
                BigDecimal baseAmount = order.getTotalAmount();
                BigDecimal systemShare = calculateSystemShare(baseAmount, commission);
                BigDecimal branchIncome = baseAmount.subtract(systemShare);
                
                totalSystemShare = totalSystemShare.add(systemShare);
                totalBranchIncome = totalBranchIncome.add(branchIncome);
            } catch (Exception e) {
                log.error("Error calculating commission for order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
        
        summary.put("systemShare", totalSystemShare);
        summary.put("branchIncome", totalBranchIncome);
        
        // Calculate commission rate
        BigDecimal commissionRate = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            commissionRate = totalSystemShare.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, java.math.RoundingMode.HALF_UP);
        }
        summary.put("commissionRate", commissionRate);
        
        // Calculate average order value safely
        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (!validOrders.isEmpty() && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            avgOrderValue = totalRevenue.divide(BigDecimal.valueOf(validOrders.size()), 2, java.math.RoundingMode.HALF_UP);
        }
        summary.put("avgOrderValue", avgOrderValue);
        
        return summary;
    }

    @Override
    public Map<String, Object> getPaymentMethodBreakdown(LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting payment method breakdown from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        
        Map<String, Object> breakdown = new HashMap<>();
        
        long onlineOrders = validOrders.stream()
                .filter(order -> order.getPaymentMethod() != null && order.getPaymentMethod() != PaymentMethod.COD)
                .count();
        
        long codOrders = validOrders.stream()
                .filter(order -> order.getPaymentMethod() == null || order.getPaymentMethod() == PaymentMethod.COD)
                .count();
        
        BigDecimal onlineRevenue = validOrders.stream()
                .filter(order -> order.getPaymentMethod() != null && order.getPaymentMethod() != PaymentMethod.COD && order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal codRevenue = validOrders.stream()
                .filter(order -> (order.getPaymentMethod() == null || order.getPaymentMethod() == PaymentMethod.COD) && order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        breakdown.put("onlineOrders", onlineOrders);
        breakdown.put("codOrders", codOrders);
        breakdown.put("onlineRevenue", onlineRevenue);
        breakdown.put("codRevenue", codRevenue);
        
        return breakdown;
    }

    @Override
    public Map<String, Object> getCancellationStats(LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting cancellation stats from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        // Get all orders in the period (including cancelled/refunded)
        List<Order> allOrders = getAllOrdersInPeriod(fromDate, toDate, branchId);
        
        long totalOrders = allOrders.size();
        long cancelledOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .count();
        long refundedOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.REFUNDED)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("cancelledOrders", cancelledOrders);
        stats.put("refundedOrders", refundedOrders);
        stats.put("cancellationRate", totalOrders == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(cancelledOrders).divide(BigDecimal.valueOf(totalOrders), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
        
        return stats;
    }

    // Helper methods
    private List<Order> getValidOrders(LocalDate fromDate, LocalDate toDate, String branchId) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepository.findByStatusAndUpdatedAtBetween(
                OrderStatus.DELIVERED, fromDateTime, toDateTime);
        
        // Filter by branch if specified
        if (branchId != null && !branchId.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getBranch().getBranchId().equals(branchId))
                    .collect(Collectors.toList());
        }
        
        // Filter by payment status (exclude REFUNDED, handle null)
        orders = orders.stream()
                .filter(order -> order.getPaymentStatus() == null || order.getPaymentStatus() != PaymentStatus.REFUNDED)
                .collect(Collectors.toList());
        
        return orders;
    }
    
    private List<Order> getAllOrdersInPeriod(LocalDate fromDate, LocalDate toDate, String branchId) {
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
        
        List<Order> orders = orderRepository.findByUpdatedAtBetween(fromDateTime, toDateTime);
        
        if (branchId != null && !branchId.isEmpty()) {
            orders = orders.stream()
                    .filter(order -> order.getBranch().getBranchId().equals(branchId))
                    .collect(Collectors.toList());
        }
        
        return orders;
    }
    
    private RevenueReportDTO calculateBranchMetrics(String branchId, List<Order> orders, LocalDate fromDate, LocalDate toDate) {
        Branch branch = branchRepository.findById(branchId).orElse(null);
        
        BigDecimal totalRevenue = orders.stream()
                .filter(order -> order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSystemShare = BigDecimal.ZERO;
        BigDecimal totalBranchIncome = BigDecimal.ZERO;
        
        for (Order order : orders) {
            try {
                if (order.getTotalAmount() == null) continue;
                
                BranchCommission commission = getCommissionForOrder(order);
                BigDecimal baseAmount = order.getTotalAmount();
                BigDecimal systemShare = calculateSystemShare(baseAmount, commission);
                BigDecimal branchIncome = baseAmount.subtract(systemShare);
                
                totalSystemShare = totalSystemShare.add(systemShare);
                totalBranchIncome = totalBranchIncome.add(branchIncome);
            } catch (Exception e) {
                log.error("Error calculating metrics for order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
        
        return RevenueReportDTO.builder()
                .branchId(branchId)
                .branchName(branch != null ? branch.getName() : "Unknown Branch")
                .totalOrders((long) orders.size())
                .totalRevenue(totalRevenue)
                .systemShare(totalSystemShare)
                .branchIncome(totalBranchIncome)
                .avgOrderValue(orders.isEmpty() ? BigDecimal.ZERO :
                        totalRevenue.divide(BigDecimal.valueOf(orders.size()), 2, java.math.RoundingMode.HALF_UP))
                .build();
    }
    
    private BranchCommission getCommissionForOrder(Order order) {
        try {
            LocalDateTime orderDate = order.getUpdatedAt() != null ? order.getUpdatedAt() : order.getCreatedAt();
            if (orderDate == null) {
                log.warn("Order {} has no date information", order.getOrderId());
                return null;
            }
            
            String branchId = order.getBranch().getBranchId();
            log.debug("Looking for commission for branch {} on date {}", branchId, orderDate);
            
            Optional<BranchCommission> commission = branchCommissionRepository.findByBranchIdAndEffectiveDate(branchId, orderDate);
            
            if (commission.isPresent()) {
                log.debug("Found commission for branch {}: type={}, value={}", 
                    branchId, commission.get().getCommissionType(), commission.get().getCommissionValue());
                return commission.get();
            } else {
                log.warn("No commission found for branch {} on date {}", branchId, orderDate);
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting commission for order {}: {}", order.getOrderId(), e.getMessage());
            return null;
        }
    }
    
    private BigDecimal calculateSystemShare(BigDecimal baseAmount, BranchCommission commission) {
        if (commission == null) {
            return BigDecimal.ZERO; // No commission configured
        }
        
        if (BranchCommission.CommissionType.PERCENT.equals(commission.getCommissionType())) {
            return baseAmount.multiply(commission.getCommissionValue())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if (BranchCommission.CommissionType.FIXED.equals(commission.getCommissionType())) {
            // Fixed amount per order, but not exceeding base amount
            return commission.getCommissionValue().min(baseAmount);
        }
        
        return BigDecimal.ZERO;
    }
    
    @Override
    public Map<String, Object> debugCommissionData() {
        Map<String, Object> debug = new HashMap<>();
        
        // Count total commissions
        long totalCommissions = branchCommissionRepository.count();
        debug.put("totalCommissions", totalCommissions);
        
        // Count active commissions
        long activeCommissions = branchCommissionRepository.countByIsActiveTrue();
        debug.put("activeCommissions", activeCommissions);
        
        // Get all active commissions
        List<BranchCommission> commissions = branchCommissionRepository.findByIsActiveTrueOrderByBranch_NameAsc();
        debug.put("commissionDetails", commissions.stream().map(c -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("commissionId", c.getCommissionId());
            detail.put("branchId", c.getBranch().getBranchId());
            detail.put("branchName", c.getBranch().getName());
            detail.put("commissionType", c.getCommissionType());
            detail.put("commissionValue", c.getCommissionValue());
            detail.put("effectiveFrom", c.getEffectiveFrom());
            detail.put("effectiveTo", c.getEffectiveTo());
            detail.put("isActive", c.getIsActive());
            return detail;
        }).collect(Collectors.toList()));
        
        // Test commission lookup for recent orders
        List<Order> recentOrders = orderRepository.findByStatusAndUpdatedAtBetween(
            OrderStatus.DELIVERED, 
            LocalDateTime.now().minusDays(30), 
            LocalDateTime.now()
        ).stream().limit(5).collect(Collectors.toList());
        
        debug.put("recentOrdersCommissionTest", recentOrders.stream().map(order -> {
            Map<String, Object> test = new HashMap<>();
            test.put("orderId", order.getOrderId());
            test.put("branchId", order.getBranch().getBranchId());
            test.put("orderDate", order.getUpdatedAt());
            
            BranchCommission commission = getCommissionForOrder(order);
            test.put("foundCommission", commission != null);
            if (commission != null) {
                test.put("commissionType", commission.getCommissionType());
                test.put("commissionValue", commission.getCommissionValue());
                test.put("calculatedShare", calculateSystemShare(order.getTotalAmount(), commission));
            }
            return test;
        }).collect(Collectors.toList()));
        
        return debug;
    }
}
