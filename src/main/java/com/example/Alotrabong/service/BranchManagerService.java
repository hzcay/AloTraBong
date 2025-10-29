package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.*;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BranchManagerService {

    // ==================== DASHBOARD ====================
    
    /**
     * Lấy dữ liệu dashboard cho chi nhánh
     */
    BranchDashboardDTO getDashboardData(String branchId);

    /**
     * Lấy thông tin chi nhánh
     */
    BranchDTO getBranchInfo(String branchId);

    /**
     * Cập nhật thông tin chi nhánh
     */
    BranchDTO updateBranchInfo(String branchId, BranchDTO branchDTO);

    // ==================== ORDER MANAGEMENT ====================

    /**
     * Lấy danh sách đơn hàng của chi nhánh
     */
    Page<OrderDTO> getOrders(String branchId, Pageable pageable, String status);

    /**
     * Lấy chi tiết đơn hàng
     */
    OrderDTO getOrderById(String orderId, String branchId);

    /**
     * Lấy thống kê đơn hàng
     */
    Map<String, Long> getOrderStats(String branchId);

    /**
     * Cập nhật trạng thái đơn hàng
     */
    OrderDTO updateOrderStatus(String orderId, String status, String branchId);

    /**
     * Phân công shipper cho đơn hàng
     */
    OrderDTO assignShipper(String orderId, String shipperId, String branchId);

    // ==================== MENU & INVENTORY MANAGEMENT ====================

    /**
     * Lấy danh sách món ăn của chi nhánh với giá và tồn kho
     */
    List<BranchMenuItemDTO> getMenuItems(String branchId);

    /**
     * Cập nhật giá món ăn
     */
    BranchMenuItemDTO updateItemPrice(String branchId, String itemId, BigDecimal newPrice);

    /**
     * Cập nhật trạng thái bán món ăn
     */
    BranchMenuItemDTO updateItemAvailability(String branchId, String itemId, Boolean isAvailable);

    /**
     * Cập nhật tồn kho
     */
    BranchMenuItemDTO updateInventory(String branchId, String itemId, Integer quantity);

    /**
     * Lấy danh sách món ăn cần cảnh báo tồn kho
     */
    List<BranchMenuItemDTO> getLowStockItems(String branchId);

    // ==================== SHIPPER MANAGEMENT ====================

    /**
     * Lấy danh sách shipper của chi nhánh
     */
    List<ShipperDTO> getShippers(String branchId);

    /**
     * Lấy shipper theo ID
     */
    ShipperDTO getShipperById(String shipperId, String branchId);

    /**
     * Cập nhật trạng thái shipper
     */
    ShipperDTO updateShipperStatus(String shipperId, Boolean isActive, String branchId);

    /**
     * Tạo shipper mới cho chi nhánh
     */
    ShipperDTO createShipper(String branchId, CreateShipperRequest request);

    /**
     * Lấy thống kê giao hàng của shipper
     */
    ShipperStatsDTO getShipperStats(String shipperId, String branchId);

    /**
     * Lấy lịch sử giao hàng của shipper
     */
    List<ShipmentDTO> getShipperDeliveryHistory(String shipperId, String branchId);

    /**
     * Lấy danh sách shipment của chi nhánh
     */
    List<ShipmentDTO> getShipments(String branchId);

    /**
     * Cập nhật trạng thái shipment
     */
    ShipmentDTO updateShipmentStatus(String shipmentId, Integer status, String branchId);

    // ==================== PROMOTION MANAGEMENT ====================

    /**
     * Lấy danh sách khuyến mãi của chi nhánh
     */
    List<PromotionDTO> getPromotions(String branchId);

    /**
     * Tạo khuyến mãi mới cho chi nhánh
     */
    PromotionDTO createPromotion(String branchId, CreatePromotionRequest request);

    /**
     * Cập nhật khuyến mãi
     */
    PromotionDTO updatePromotion(String promotionId, PromotionDTO promotionDTO, String branchId);

    /**
     * Xóa/ngừng khuyến mãi
     */
    void deactivatePromotion(String promotionId, String branchId);

    // ==================== REVENUE REPORTS ====================

    /**
     * Lấy báo cáo doanh thu chi nhánh
     */
    BranchRevenueReportDTO getRevenueReport(String branchId, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy báo cáo doanh thu theo ngày
     */
    List<DailyRevenueDTO> getDailyRevenue(String branchId, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy top sản phẩm bán chạy
     */
    List<TopSellingItemDTO> getTopSellingItems(String branchId, LocalDate startDate, LocalDate endDate);

    // ==================== SHIPPING RATES ====================

    /**
     * Lấy phí giao hàng của chi nhánh
     */
    ShippingRateDTO getShippingRates(String branchId);

    /**
     * Cập nhật phí giao hàng
     */
    ShippingRateDTO updateShippingRates(String branchId, ShippingRateDTO ratesDTO);

    // ==================== NOTIFICATIONS ====================

    /**
     * Lấy thông báo cho chi nhánh
     */
    List<BranchNotificationDTO> getNotifications(String branchId);

    /**
     * Đánh dấu thông báo đã đọc
     */
    void markNotificationAsRead(String notificationId, String branchId);

    // ==================== ITEM MANAGEMENT ====================
    
    /**
     * Lấy danh sách items chưa có trong chi nhánh
     */
    List<ItemDTO> getAvailableItemsNotInBranch(String branchId);
    
    /**
     * Thêm item vào chi nhánh với giá và tồn kho
     */
    BranchMenuItemDTO addItemToBranch(String branchId, AddItemToBranchRequest request);
    
}
