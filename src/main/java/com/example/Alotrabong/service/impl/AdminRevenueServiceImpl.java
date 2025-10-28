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
                .collect(Collectors.groupingBy(order -> {
                    LocalDateTime orderDate = order.getUpdatedAt(); // Use updated_at when status changed to DELIVERED
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
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdIn(validOrderIds);
        
        // Group by item
        Map<String, List<OrderItem>> itemsByItemId = orderItems.stream()
                .collect(Collectors.groupingBy(item -> item.getItem().getItemId()));
        
        List<RevenueReportDTO> reports = new ArrayList<>();
        
        for (Map.Entry<String, List<OrderItem>> entry : itemsByItemId.entrySet()) {
            String itemId = entry.getKey();
            List<OrderItem> itemOrders = entry.getValue();
            
            Item item = itemRepository.findById(itemId).orElse(null);
            if (item == null) continue;
            
            RevenueReportDTO report = RevenueReportDTO.builder()
                    .itemId(itemId)
                    .itemName(item.getName())
                    .itemQuantity(itemOrders.stream()
                            .mapToLong(OrderItem::getQuantity)
                            .sum())
                    .itemRevenue(itemOrders.stream()
                            .map(OrderItem::getLineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
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
        
        // Basic metrics
        summary.put("totalOrders", validOrders.size());
        summary.put("totalRevenue", validOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        
        // Calculate system share and branch income
        BigDecimal totalSystemShare = BigDecimal.ZERO;
        BigDecimal totalBranchIncome = BigDecimal.ZERO;
        
        for (Order order : validOrders) {
            BranchCommission commission = getCommissionForOrder(order);
            BigDecimal baseAmount = order.getTotalAmount(); // Assuming shipping fee is not deducted
            BigDecimal systemShare = calculateSystemShare(baseAmount, commission);
            BigDecimal branchIncome = baseAmount.subtract(systemShare);
            
            totalSystemShare = totalSystemShare.add(systemShare);
            totalBranchIncome = totalBranchIncome.add(branchIncome);
        }
        
        summary.put("systemShare", totalSystemShare);
        summary.put("branchIncome", totalBranchIncome);
                summary.put("avgOrderValue", validOrders.isEmpty() ? BigDecimal.ZERO :
                summary.get("totalRevenue").toString().equals("0") ? BigDecimal.ZERO :
                ((BigDecimal) summary.get("totalRevenue")).divide(BigDecimal.valueOf(validOrders.size()), 2, java.math.RoundingMode.HALF_UP));
        
        return summary;
    }

    @Override
    public Map<String, Object> getPaymentMethodBreakdown(LocalDate fromDate, LocalDate toDate, String branchId) {
        log.info("Getting payment method breakdown from {} to {}, branchId: {}", fromDate, toDate, branchId);
        
        List<Order> validOrders = getValidOrders(fromDate, toDate, branchId);
        
        Map<String, Object> breakdown = new HashMap<>();
        
        long onlineOrders = validOrders.stream()
                .filter(order -> order.getPaymentMethod() != PaymentMethod.COD)
                .count();
        
        long codOrders = validOrders.stream()
                .filter(order -> order.getPaymentMethod() == PaymentMethod.COD)
                .count();
        
        BigDecimal onlineRevenue = validOrders.stream()
                .filter(order -> order.getPaymentMethod() != PaymentMethod.COD)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal codRevenue = validOrders.stream()
                .filter(order -> order.getPaymentMethod() == PaymentMethod.COD)
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
        
        // Filter by payment status (exclude REFUNDED)
        orders = orders.stream()
                .filter(order -> order.getPaymentStatus() != PaymentStatus.REFUNDED)
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
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSystemShare = BigDecimal.ZERO;
        BigDecimal totalBranchIncome = BigDecimal.ZERO;
        
        for (Order order : orders) {
            BranchCommission commission = getCommissionForOrder(order);
            BigDecimal baseAmount = order.getTotalAmount();
            BigDecimal systemShare = calculateSystemShare(baseAmount, commission);
            BigDecimal branchIncome = baseAmount.subtract(systemShare);
            
            totalSystemShare = totalSystemShare.add(systemShare);
            totalBranchIncome = totalBranchIncome.add(branchIncome);
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
        LocalDateTime orderDate = order.getUpdatedAt();
        
        return branchCommissionRepository.findByBranchIdAndEffectiveDate(
                order.getBranch().getBranchId(), orderDate)
                .orElse(null);
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
}
