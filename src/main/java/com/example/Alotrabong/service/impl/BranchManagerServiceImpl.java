package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.*;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.BranchManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BranchManagerServiceImpl implements BranchManagerService {

    private final BranchRepository branchRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BranchItemPriceRepository branchItemPriceRepository;
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final ShipperRepository shipperRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final ShippingRateRepository shippingRateRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    // ==================== DASHBOARD ====================

    @Override
    @Transactional(readOnly = true)
    public BranchDashboardDTO getDashboardData(String branchId) {
        log.info("Getting dashboard data for branch: {}", branchId);
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Thống kê đơn hàng
        Long totalOrders = orderRepository.countByBranch_BranchId(branchId);
        Long pendingOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.PENDING);
        Long completedOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.DELIVERED);
        Long cancelledOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.CANCELLED);

        // Thống kê doanh thu
        BigDecimal todayRevenue = orderRepository.getTodayRevenue(branchId);
        BigDecimal monthlyRevenue = orderRepository.getMonthlyRevenue(branchId);
        BigDecimal avgOrderValue = orderRepository.getAvgOrderValue(branchId);

        // Thống kê shipper
        Long totalShippers = (long) shipperRepository.findByBranch_BranchId(branchId).size();
        Long activeShippers = (long) shipperRepository.findByBranch_BranchId(branchId).stream()
                .filter(shipper -> Boolean.TRUE.equals(shipper.getIsActive()))
                .count();

        // Cảnh báo tồn kho
        List<BranchMenuItemDTO> lowStockItems = getLowStockItems(branchId);
        Long lowStockItemsCount = (long) lowStockItems.size();

        // Đơn hàng gần đây
        List<OrderDTO> recentOrders = orderRepository.findByBranchOrderByCreatedAtDesc(branchRepository.findById(branchId).orElse(null))
                .stream()
                .limit(5)
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());

        // Khuyến mãi đang hoạt động
        List<PromotionDTO> activePromotions = promotionRepository.findByBranchIdAndIsActiveTrue(branchId)
                .stream()
                .map(this::convertToPromotionDTO)
                .collect(Collectors.toList());

        return BranchDashboardDTO.builder()
                .branchId(branchId)
                .branchName(branch.getName())
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .avgOrderValue(avgOrderValue != null ? avgOrderValue : BigDecimal.ZERO)
                .totalShippers(totalShippers)
                .activeShippers(activeShippers)
                .busyShippers(0L) // TODO: Calculate busy shippers
                .lowStockItems(lowStockItemsCount)
                .lowStockItemsList(lowStockItems)
                .recentOrders(recentOrders)
                .activePromotions(activePromotions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchDTO getBranchInfo(String branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        return convertToBranchDTO(branch);
    }

    @Override
    public BranchDTO updateBranchInfo(String branchId, BranchDTO branchDTO) {
        log.info("Updating branch info: {}", branchId);
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        branch.setName(branchDTO.getName());
        branch.setAddress(branchDTO.getAddress());
        branch.setPhone(branchDTO.getPhone());
        // branch.setOpenHours(branchDTO.getOpenHours()); // TODO: Add openHours field to BranchDTO
        branch.setIsActive(branchDTO.getIsActive());

        branch = branchRepository.save(branch);
        return convertToBranchDTO(branch);
    }

    // ==================== ORDER MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrders(String branchId, Pageable pageable, String status) {
        log.info("Getting orders for branch: {}, status: {}", branchId, status);
        
        Branch branch = branchRepository.findById(branchId).orElse(null);
        if (branch == null) {
            return Page.empty(pageable);
        }
        
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByBranchAndStatus(branch, orderStatus, pageable)
                    .map(this::convertToOrderDTO);
        }
        
        return orderRepository.findByBranch(branch, pageable)
                .map(this::convertToOrderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String orderId, String branchId) {
        Order order = orderRepository.findByOrderIdAndBranch_BranchId(orderId, branchId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found");
        }
        return convertToOrderDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStats(String branchId) {
        log.info("Getting order stats for branch: {}", branchId);
        
        Map<String, Long> stats = new HashMap<>();
        
        // Tổng đơn hàng
        Long totalOrders = orderRepository.countByBranch_BranchId(branchId);
        stats.put("totalOrders", totalOrders);
        
        // Đơn hàng chờ xử lý
        Long pendingOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.PENDING);
        stats.put("pendingOrders", pendingOrders);
        
        // Đơn hàng đang xử lý (CONFIRMED, PREPARING, READY, DELIVERING)
        Long processingOrders = 
            orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.CONFIRMED) +
            orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.PREPARING) +
            orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.READY) +
            orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.DELIVERING);
        stats.put("processingOrders", processingOrders);
        
        // Đơn hàng hoàn thành
        Long completedOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.DELIVERED);
        stats.put("completedOrders", completedOrders);
        
        // Đơn hàng đã hủy
        Long cancelledOrders = orderRepository.countByBranch_BranchIdAndStatus(branchId, OrderStatus.CANCELLED);
        stats.put("cancelledOrders", cancelledOrders);
        
        return stats;
    }

    @Override
    public OrderDTO updateOrderStatus(String orderId, String status, String branchId) {
        log.info("Updating order status: {} to {}", orderId, status);
        
        Order order = orderRepository.findByOrderIdAndBranch_BranchId(orderId, branchId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found");
        }

        OrderStatus oldStatus = order.getStatus();
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        
        // Trừ tồn kho khi đơn hàng chuyển sang READY (đồ ăn sẵn sàng)
        if (orderStatus == OrderStatus.READY && oldStatus != OrderStatus.READY) {
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            for (OrderItem orderItem : orderItems) {
                Inventory inventory = inventoryRepository
                        .findByBranch_BranchIdAndItem_ItemId(branchId, orderItem.getItem().getItemId())
                        .orElse(null);
                
                if (inventory != null) {
                    // Kiểm tra tồn kho trước khi trừ
                    if (inventory.getQuantity() < orderItem.getQuantity()) {
                        throw new RuntimeException("Insufficient inventory for item: " + orderItem.getItem().getName() + 
                            ". Available: " + inventory.getQuantity() + ", Required: " + orderItem.getQuantity());
                    }
                    
                    inventory.setQuantity(inventory.getQuantity() - orderItem.getQuantity());
                    inventoryRepository.save(inventory);
                    
                    log.info("Deducted {} units of item {} from branch inventory (Order READY). Remaining: {}", 
                        orderItem.getQuantity(), orderItem.getItem().getName(), inventory.getQuantity());
                }
            }
        }
        
        order.setStatus(orderStatus);
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);
        return convertToOrderDTO(order);
    }

    @Override
    public OrderDTO assignShipper(String orderId, String shipperId, String branchId) {
        log.info("Assigning shipper {} to order {}", shipperId, orderId);
        
        Order order = orderRepository.findByOrderIdAndBranch_BranchId(orderId, branchId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found");
        }

        Shipper shipper = shipperRepository.findByShipperIdAndBranch_BranchId(shipperId, branchId);
        if (shipper == null) {
            throw new ResourceNotFoundException("Shipper not found");
        }

        // Tạo shipment
        Shipment shipment = Shipment.builder()
                .order(order)
                .shipper(shipper)
                .status(0) // Assigned
                .build();

        shipmentRepository.save(shipment);

        // Chỉ cập nhật trạng thái nếu đơn hàng chưa được xác nhận
        // Không quay ngược trạng thái nếu đã ở trạng thái cao hơn
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        // Nếu đơn hàng đã READY thì có thể chuyển sang DELIVERING
        else if (order.getStatus() == OrderStatus.READY) {
            order.setStatus(OrderStatus.DELIVERING);
        }
        // Các trạng thái khác giữ nguyên
        
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        return convertToOrderDTO(order);
    }

    // ==================== MENU & INVENTORY MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuItemDTO> getMenuItems(String branchId) {
        log.info("Getting menu items for branch: {}", branchId);
        
        return branchItemPriceRepository.findByBranch_BranchId(branchId)
                .stream()
                .map(this::convertToBranchMenuItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BranchMenuItemDTO updateItemPrice(String branchId, String itemId, BigDecimal newPrice) {
        log.info("Updating item price: {} to {} for branch: {}", itemId, newPrice, branchId);
        
        BranchItemPrice branchItemPrice = branchItemPriceRepository
                .findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in branch"));

        branchItemPrice.setPrice(newPrice);
        branchItemPrice = branchItemPriceRepository.save(branchItemPrice);

        return convertToBranchMenuItemDTO(branchItemPrice);
    }

    @Override
    public BranchMenuItemDTO updateItemAvailability(String branchId, String itemId, Boolean isAvailable) {
        log.info("Updating item availability: {} to {} for branch: {}", itemId, isAvailable, branchId);
        
        BranchItemPrice branchItemPrice = branchItemPriceRepository
                .findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in branch"));

        branchItemPrice.setIsAvailable(isAvailable);
        branchItemPrice = branchItemPriceRepository.save(branchItemPrice);

        return convertToBranchMenuItemDTO(branchItemPrice);
    }

    @Override
    public BranchMenuItemDTO updateInventory(String branchId, String itemId, Integer quantity) {
        log.info("Updating inventory: {} to {} for branch: {}", itemId, quantity, branchId);
        
        Inventory inventory = inventoryRepository
                .findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        inventory.setQuantity(quantity);
        inventory = inventoryRepository.save(inventory);

        // Convert to BranchMenuItemDTO
        BranchItemPrice branchItemPrice = branchItemPriceRepository
                .findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in branch"));

        return convertToBranchMenuItemDTO(branchItemPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchMenuItemDTO> getLowStockItems(String branchId) {
        log.info("Getting low stock items for branch: {}", branchId);
        
        return inventoryRepository.findLowStockItems(branchId)
                .stream()
                .map(inventory -> {
                    BranchItemPrice branchItemPrice = branchItemPriceRepository
                            .findByBranch_BranchIdAndItem_ItemId(branchId, inventory.getItem().getItemId())
                            .orElse(null);
                    
                    if (branchItemPrice != null) {
                        BranchMenuItemDTO dto = convertToBranchMenuItemDTO(branchItemPrice);
                        dto.setIsLowStock(true);
                        return dto;
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    // ==================== SHIPPER MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<ShipperDTO> getShippers(String branchId) {
        log.info("Getting shippers for branch: {}", branchId);
        
        return shipperRepository.findByBranch_BranchId(branchId)
                .stream()
                .map(this::convertToShipperDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperDTO getShipperById(String shipperId, String branchId) {
        Shipper shipper = shipperRepository.findByShipperIdAndBranch_BranchId(shipperId, branchId);
        if (shipper == null) {
            throw new ResourceNotFoundException("Shipper not found");
        }
        return convertToShipperDTO(shipper);
    }

    @Override
    public ShipperDTO updateShipperStatus(String shipperId, Boolean isActive, String branchId) {
        log.info("Updating shipper status: {} to {} for branch: {}", shipperId, isActive, branchId);
        
        Shipper shipper = shipperRepository.findByShipperIdAndBranch_BranchId(shipperId, branchId);
        if (shipper == null) {
            throw new ResourceNotFoundException("Shipper not found");
        }

        shipper.setIsActive(isActive);
        shipper = shipperRepository.save(shipper);

        return convertToShipperDTO(shipper);
    }

    @Override
    public ShipperDTO createShipper(String branchId, CreateShipperRequest request) {
        log.info("Creating shipper for branch: {} with email: {}", branchId, request.getUserEmail());
        
        // Find branch
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        // Find user by email
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getUserEmail()));
        
        // Check if user is already a shipper
        if (shipperRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("User is already a shipper");
        }
        
        // Check if user already has SHIPPER role for this branch
        Role shipperRole = roleRepository.findByRoleCode(RoleCode.SHIPPER)
                .orElseThrow(() -> new RuntimeException("SHIPPER role not found in database"));
        
        boolean hasShipperRoleForBranch = userRoleRepository.findByUser(user)
                .stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.SHIPPER && 
                               ur.getBranch() != null && 
                               ur.getBranch().getBranchId().equals(branchId));
        
        // Create shipper
        Shipper shipper = Shipper.builder()
                .user(user)
                .branch(branch)
                .vehiclePlate(request.getVehiclePlate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        shipper = shipperRepository.save(shipper);
        
        // Create UserRole if not exists for this branch
        if (!hasShipperRoleForBranch) {
            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(shipperRole)
                    .branch(branch) // Assign to specific branch
                    .build();
            userRoleRepository.save(userRole);
            log.info("Created SHIPPER role for user: {}", user.getEmail());
        }
        
        log.info("Shipper created successfully: {}", shipper.getShipperId());
        
        return convertToShipperDTO(shipper);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperStatsDTO getShipperStats(String shipperId, String branchId) {
        log.info("Getting stats for shipper: {} in branch: {}", shipperId, branchId);
        
        Shipper shipper = shipperRepository.findByShipperIdAndBranch_BranchId(shipperId, branchId);
        if (shipper == null) {
            throw new ResourceNotFoundException("Shipper not found");
        }
        
        // Get all shipments for this shipper
        List<Shipment> shipments = shipmentRepository.findByShipper(shipper);
        
        long totalDeliveries = shipments.size();
        long successfulDeliveries = shipments.stream()
                .filter(s -> s.getStatus() == 2) // Status 2 = Delivered
                .count();
        long currentDeliveries = shipments.stream()
                .filter(s -> s.getStatus() == 1) // Status 1 = Delivering
                .count();
        long cancelledDeliveries = shipments.stream()
                .filter(s -> s.getStatus() == 3) // Status 3 = Cancelled
                .count();
        
        BigDecimal successRate = totalDeliveries > 0 ? 
                BigDecimal.valueOf(successfulDeliveries * 100.0 / totalDeliveries).setScale(2, java.math.RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        BigDecimal totalDistance = shipments.stream()
                .filter(s -> s.getDistanceKm() != null)
                .map(Shipment::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return ShipperStatsDTO.builder()
                .shipperId(shipperId)
                .totalDeliveries(totalDeliveries)
                .successfulDeliveries(successfulDeliveries)
                .currentDeliveries(currentDeliveries)
                .cancelledDeliveries(cancelledDeliveries)
                .successRate(successRate)
                .totalDistance(totalDistance)
                .averageDeliveryTime(BigDecimal.ZERO) // TODO: Calculate average delivery time
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDTO> getShipperDeliveryHistory(String shipperId, String branchId) {
        log.info("Getting delivery history for shipper: {} in branch: {}", shipperId, branchId);
        
        Shipper shipper = shipperRepository.findByShipperIdAndBranch_BranchId(shipperId, branchId);
        if (shipper == null) {
            throw new ResourceNotFoundException("Shipper not found");
        }
        
        return shipmentRepository.findByShipperOrderByCreatedAtDesc(shipper)
                .stream()
                .map(this::convertToShipmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentDTO> getShipments(String branchId) {
        log.info("Getting shipments for branch: {}", branchId);
        
        return shipmentRepository.findByShipper_Branch_BranchId(branchId)
                .stream()
                .map(this::convertToShipmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentDTO updateShipmentStatus(String shipmentId, Integer status, String branchId) {
        log.info("Updating shipment status: {} to {} for branch: {}", shipmentId, status, branchId);
        
        Shipment shipment = shipmentRepository.findByShipmentIdAndShipper_Branch_BranchId(shipmentId, branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        shipment.setStatus(status);
        
        if (status == 2) { // Delivered
            shipment.setDeliveredTime(LocalDateTime.now());
        }
        
        shipment = shipmentRepository.save(shipment);

        // Tạo shipment event
        ShipmentEvent event = ShipmentEvent.builder()
                .shipment(shipment)
                .status(status)
                .eventTime(LocalDateTime.now())
                .build();
        
        shipmentEventRepository.save(event);

        return convertToShipmentDTO(shipment);
    }

    // ==================== PROMOTION MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDTO> getPromotions(String branchId) {
        log.info("Getting promotions for branch: {}", branchId);
        
        return promotionRepository.findByBranchId(branchId)
                .stream()
                .map(this::convertToPromotionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDTO createPromotion(String branchId, CreatePromotionRequest request) {
        log.info("Creating promotion for branch: {}", branchId);
        
        Promotion promotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .branchId(branchId)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .build();

        promotion = promotionRepository.save(promotion);
        return convertToPromotionDTO(promotion);
    }

    @Override
    public PromotionDTO updatePromotion(String promotionId, PromotionDTO promotionDTO, String branchId) {
        log.info("Updating promotion: {} for branch: {}", promotionId, branchId);
        
        Promotion promotion = promotionRepository.findByPromotionIdAndBranchId(promotionId, branchId);
        if (promotion == null) {
            throw new ResourceNotFoundException("Promotion not found");
        }

        promotion.setName(promotionDTO.getName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setDiscountType(promotionDTO.getDiscountType());
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());

        promotion = promotionRepository.save(promotion);
        return convertToPromotionDTO(promotion);
    }

    @Override
    public void deactivatePromotion(String promotionId, String branchId) {
        log.info("Deactivating promotion: {} for branch: {}", promotionId, branchId);
        
        Promotion promotion = promotionRepository.findByPromotionIdAndBranchId(promotionId, branchId);

        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }

    // ==================== REVENUE REPORTS ====================

    @Override
    @Transactional(readOnly = true)
    public BranchRevenueReportDTO getRevenueReport(String branchId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting revenue report for branch: {} from {} to {}", branchId, startDate, endDate);
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // Thống kê đơn hàng
        Long totalOrders = orderRepository.countByBranchAndDateRange(branchId, startDate, endDate);
        Long completedOrders = orderRepository.countByBranchAndStatusAndDateRange(branchId, OrderStatus.DELIVERED, startDate, endDate);
        Long cancelledOrders = orderRepository.countByBranchAndStatusAndDateRange(branchId, OrderStatus.CANCELLED, startDate, endDate);

        // Thống kê doanh thu
        BigDecimal totalRevenue = orderRepository.getRevenueByBranchAndDateRange(branchId, startDate, endDate);
        BigDecimal completedRevenue = orderRepository.getRevenueByBranchAndStatusAndDateRange(branchId, OrderStatus.DELIVERED, startDate, endDate);
        BigDecimal avgOrderValue = totalOrders > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Top sản phẩm bán chạy
        List<TopSellingItemDTO> topSellingItems = getTopSellingItems(branchId, startDate, endDate);

        // Doanh thu theo ngày
        List<DailyRevenueDTO> dailyRevenue = getDailyRevenue(branchId, startDate, endDate);

        // Tỷ lệ hoàn thành
        BigDecimal completionRate = totalOrders > 0 ? 
                BigDecimal.valueOf(completedOrders).divide(BigDecimal.valueOf(totalOrders), 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;

        return BranchRevenueReportDTO.builder()
                .branchId(branchId)
                .branchName(branch.getName())
                .startDate(startDate)
                .endDate(endDate)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .completedRevenue(completedRevenue != null ? completedRevenue : BigDecimal.ZERO)
                .avgOrderValue(avgOrderValue)
                .topSellingItems(topSellingItems)
                .dailyRevenue(dailyRevenue)
                .completionRate(completionRate)
                .cancellationRate(BigDecimal.valueOf(100).subtract(completionRate))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyRevenueDTO> getDailyRevenue(String branchId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement daily revenue calculation
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingItemDTO> getTopSellingItems(String branchId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement top selling items calculation
        return List.of();
    }

    // ==================== SHIPPING RATES ====================

    @Override
    @Transactional(readOnly = true)
    public ShippingRateDTO getShippingRates(String branchId) {
        ShippingRate rate = shippingRateRepository.findActiveByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found"));
        return convertToShippingRateDTO(rate);
    }

    @Override
    public ShippingRateDTO updateShippingRates(String branchId, ShippingRateDTO ratesDTO) {
        log.info("Updating shipping rates for branch: {}", branchId);
        
        ShippingRate rate = shippingRateRepository.findActiveByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rate not found"));

        rate.setBaseFee(ratesDTO.getBaseFee());
        rate.setPerKmFee(ratesDTO.getPerKmFee());
        rate.setFreeShipThreshold(ratesDTO.getFreeShipThreshold());

        rate = shippingRateRepository.save(rate);
        return convertToShippingRateDTO(rate);
    }

    // ==================== NOTIFICATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public List<BranchNotificationDTO> getNotifications(String branchId) {
        // TODO: Implement notifications system
        return List.of();
    }

    @Override
    public void markNotificationAsRead(String notificationId, String branchId) {
        // TODO: Implement notification read status
    }

    // ==================== ITEM MANAGEMENT ====================

    @Override
    @Transactional(readOnly = true)
    public List<ItemDTO> getAvailableItemsNotInBranch(String branchId) {
        log.info("Getting available items not in branch: {}", branchId);
        
        // Lấy tất cả items active
        List<Item> allItems = itemRepository.findByIsActive(true);
        
        // Lấy danh sách items đã có trong chi nhánh
        List<String> existingItemIds = branchItemPriceRepository.findByBranch_BranchId(branchId)
                .stream()
                .map(bip -> bip.getItem().getItemId())
                .collect(Collectors.toList());
        
        // Filter ra những items chưa có trong chi nhánh
        return allItems.stream()
                .filter(item -> !existingItemIds.contains(item.getItemId()))
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BranchMenuItemDTO addItemToBranch(String branchId, AddItemToBranchRequest request) {
        log.info("Adding item {} to branch {} with price {}", request.getItemId(), branchId, request.getSalePrice());
        
        // Validate branch và item
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        
        // Kiểm tra item đã tồn tại trong chi nhánh chưa
        if (branchItemPriceRepository.findByBranch_BranchIdAndItem_ItemId(branchId, request.getItemId()).isPresent()) {
            throw new RuntimeException("Item already exists in this branch");
        }
        
        // Tạo BranchItemPrice
        BranchItemPrice branchItemPrice = BranchItemPrice.builder()
                .branch(branch)
                .item(item)
                .price(request.getSalePrice() != null ? request.getSalePrice() : item.getPrice())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .build();
        
        branchItemPrice = branchItemPriceRepository.save(branchItemPrice);
        
        // Tạo Inventory
        Inventory inventory = Inventory.builder()
                .branch(branch)
                .item(item)
                .quantity(request.getInitialQuantity() != null ? request.getInitialQuantity() : 0)
                .safetyStock(request.getSafetyStock() != null ? request.getSafetyStock() : 10)
                .build();
        
        inventoryRepository.save(inventory);
        
        log.info("Successfully added item {} to branch {}", request.getItemId(), branchId);
        
        return convertToBranchMenuItemDTO(branchItemPrice);
    }

    // ==================== CONVERSION METHODS ====================

    private BranchDTO convertToBranchDTO(Branch branch) {
        return BranchDTO.builder()
                .branchId(branch.getBranchId())
                .name(branch.getName())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .isActive(branch.getIsActive())
                .build();
    }

    private OrderDTO convertToOrderDTO(Order order) {
        // Get order items
        List<OrderItemDTO> orderItems = orderItemRepository.findByOrder(order).stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());
        
        // Get shipper info if assigned
        String shipperName = null;
        String shipperPhone = null;
        try {
            Shipment shipment = shipmentRepository.findByOrder(order).stream().findFirst().orElse(null);
            if (shipment != null && shipment.getShipper() != null) {
                shipperName = shipment.getShipper().getUser().getFullName();
                shipperPhone = shipment.getShipper().getUser().getPhone();
            }
        } catch (Exception e) {
            log.debug("No shipper assigned to order: {}", order.getOrderId());
        }
        
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .branchId(order.getBranch().getBranchId())
                .status(order.getStatus().toString())
                .totalAmount(order.getTotalAmount())
                .grandTotal(order.getTotalAmount())
                .deliveryAddress(order.getShippingAddress())
                .deliveryPhone(order.getUser().getPhone())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : null)
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getUser().getPhone())
                .shipperName(shipperName)
                .shipperPhone(shipperPhone)
                .orderItems(orderItems)
                .items(orderItems) // Alias for frontend
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .orderItemId(orderItem.getOrderItemId())
                .itemId(orderItem.getItem().getItemId())
                .itemName(orderItem.getItem().getName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build();
    }

    private BranchMenuItemDTO convertToBranchMenuItemDTO(BranchItemPrice branchItemPrice) {
        Item item = branchItemPrice.getItem();
        Inventory inventory = inventoryRepository
                .findByBranch_BranchIdAndItem_ItemId(branchItemPrice.getBranch().getBranchId(), item.getItemId())
                .orElse(null);

        return BranchMenuItemDTO.builder()
                .itemId(item.getItemId())
                .itemName(item.getName())
                .itemCode(item.getItemCode())
                .description(item.getDescription())
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : "")
                .basePrice(item.getPrice())
                .salePrice(branchItemPrice.getPrice())
                .isAvailable(branchItemPrice.getIsAvailable())
                .quantity(inventory != null ? inventory.getQuantity() : 0)
                .safetyStock(inventory != null ? inventory.getSafetyStock() : 0)
                .isLowStock(inventory != null && inventory.getQuantity() <= inventory.getSafetyStock())
                .build();
    }

    private ShipperDTO convertToShipperDTO(Shipper shipper) {
        // Get delivery stats
        List<Shipment> shipments = shipmentRepository.findByShipper(shipper);
        int totalDeliveries = shipments.size();
        int currentDeliveries = (int) shipments.stream().filter(s -> s.getStatus() == 1).count();
        int successfulDeliveries = (int) shipments.stream().filter(s -> s.getStatus() == 2).count();
        double successRate = totalDeliveries > 0 ? (successfulDeliveries * 100.0 / totalDeliveries) : 0.0;

        return ShipperDTO.builder()
                .shipperId(shipper.getShipperId())
                .userId(shipper.getUser().getUserId())
                .fullName(shipper.getUser().getFullName())
                .email(shipper.getUser().getEmail())
                .phone(shipper.getUser().getPhone())
                .branchId(shipper.getBranch().getBranchId())
                .branchName(shipper.getBranch().getName())
                .vehiclePlate(shipper.getVehiclePlate())
                .isActive(shipper.getIsActive())
                .totalDeliveries(totalDeliveries)
                .currentDeliveries(currentDeliveries)
                .successfulDeliveries(successfulDeliveries)
                .successRate(successRate)
                .createdAt(shipper.getCreatedAt())
                .updatedAt(shipper.getUpdatedAt())
                .build();
    }

    private ShipmentDTO convertToShipmentDTO(Shipment shipment) {
        return ShipmentDTO.builder()
                .shipmentId(shipment.getShipmentId())
                .orderId(shipment.getOrder().getOrderId())
                .shipperId(shipment.getShipper().getShipperId())
                .shipperName(shipment.getShipper().getUser().getFullName())
                .shipperPhone(shipment.getShipper().getUser().getPhone())
                .status(shipment.getStatus())
                .statusText(getStatusText(shipment.getStatus()))
                .pickupTime(shipment.getPickupTime())
                .deliveredTime(shipment.getDeliveredTime())
                .distanceKm(shipment.getDistanceKm())
                .build();
    }

    private PromotionDTO convertToPromotionDTO(Promotion promotion) {
        return PromotionDTO.builder()
                .promotionId(promotion.getPromotionId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .branchId(promotion.getBranchId())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .build();
    }

    private ShippingRateDTO convertToShippingRateDTO(ShippingRate rate) {
        return ShippingRateDTO.builder()
                .rateId(rate.getRateId())
                .branchId(rate.getBranch().getBranchId())
                .baseFee(rate.getBaseFee())
                .perKmFee(rate.getPerKmFee())
                .freeShipThreshold(rate.getFreeShipThreshold())
                .isActive(rate.getIsActive())
                .build();
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "Đã phân công";
            case 1: return "Đang giao";
            case 2: return "Đã giao";
            case 3: return "Hủy";
            default: return "Không xác định";
        }
    }

    private ItemDTO convertToItemDTO(Item item) {
        return ItemDTO.builder()
                .itemId(item.getItemId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getName() : null)
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
