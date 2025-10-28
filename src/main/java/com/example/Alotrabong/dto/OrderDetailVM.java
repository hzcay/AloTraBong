package com.example.Alotrabong.dto;

import com.example.Alotrabong.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * View model cho trang chi tiết 1 đơn hàng.
 * Đây là data sạch để render ra Thymeleaf, không expose entity thô.
 */
public record OrderDetailVM(
        String code,                     // mã đơn
        OrderStatus status,              // trạng thái hiện tại
        LocalDateTime createdAt,         // thời gian tạo
        String branchName,               // chi nhánh xử lý / chuẩn bị
        String deliveryAddress,          // địa chỉ giao (nếu là giao hàng)
        String customerNote,             // ghi chú của khách
        String paymentMethod,            // kiểu thanh toán
        List<ItemVM> items,              // danh sách món
        BigDecimal subtotal,             // tổng tiền món
        BigDecimal shippingFee,          // phí ship
        BigDecimal discount,             // giảm giá
        BigDecimal grandTotal,           // tổng cuối
        boolean cancellable              // còn được hủy không
) {
    public record ItemVM(
            String name,            // tên món
            Integer qty,            // số lượng
            BigDecimal unitPrice,   // giá từng cái
            BigDecimal lineTotal,   // tiền dòng = qty * unitPrice
            String thumbnailUrl     // ảnh
    ) { }
}
