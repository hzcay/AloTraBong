package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.OrderDetailVM;
import com.example.Alotrabong.dto.OrderHistoryVM;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderItem;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderRepository orderRepo;

    // === Rule ảnh demo: đổi nếu bạn muốn source khác ===
    private static final String PLACEHOLDER_URL = "/images/placeholder.png";
    private static final String ITEM_THUMB_TEMPLATE = "/uploads/items/%s/thumb.jpg";

    /*
     * =========================================================
     * 1. LIST LỊCH SỬ ĐƠN (trang /user/order/history)
     * =========================================================
     */
    public Page<OrderHistoryVM> getHistory(
            User user,
            OrderStatus status,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        LocalDateTime start = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime endExclusive = (to != null) ? to.plusDays(1).atStartOfDay() : null;

        Page<Order> orders;
        if (start != null && endExclusive != null) {
            orders = (status != null)
                    ? orderRepo.findByUserAndStatusAndCreatedAtBetween(user, status, start, endExclusive, pageable)
                    : orderRepo.findByUserAndCreatedAtBetween(user, start, endExclusive, pageable);
        } else {
            orders = (status != null)
                    ? orderRepo.findByUserAndStatus(user, status, pageable)
                    : orderRepo.findByUser(user, pageable);
        }

        return orders.map(this::toHistoryVM);
    }

    private OrderHistoryVM toHistoryVM(Order o) {
        List<OrderHistoryVM.ItemVM> items = o.getItems().stream()
                .limit(5) // rút gọn list cho UI lịch sử
                .map(this::toHistoryItemVM)
                .toList();

        return new OrderHistoryVM(
                o.getOrderId(), // code
                o.getCreatedAt(), // createdAt
                safeBranchName(o), // branchName
                o.getStatus(), // status
                nz(o.getTotalAmount()), // grandTotal
                items);
    }

    private OrderHistoryVM.ItemVM toHistoryItemVM(OrderItem oi) {
        BigDecimal unitPrice = nz(oi.getUnitPrice());
        int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;
        BigDecimal subtotal = (oi.getLineTotal() != null)
                ? oi.getLineTotal()
                : unitPrice.multiply(BigDecimal.valueOf(qty));

        return new OrderHistoryVM.ItemVM(
                safeItemName(oi),
                qty,
                unitPrice,
                subtotal,
                thumb(oi));
    }

    /*
     * =========================================================
     * 2. CHI TIẾT 1 ĐƠN (trang /user/order/detail/{code})
     * =========================================================
     */
    public OrderDetailVM getOrderDetailForUser(User user, String code) {

        // tìm Order theo orderId/code
        Order order = orderRepo.findFirstByOrderId(code);
        if (order == null) {
            return null;
        }

        // chặn user khác đọc đơn không phải của mình
        if (order.getUser() == null || !order.getUser().equals(user)) {
            return null;
        }

        // map từng item chi tiết (không limit)
        List<OrderDetailVM.ItemVM> itemVMs = order.getItems().stream()
                .map(oi -> {
                    BigDecimal unitPrice = nz(oi.getUnitPrice());
                    int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;
                    BigDecimal lineTotal = (oi.getLineTotal() != null)
                            ? oi.getLineTotal()
                            : unitPrice.multiply(BigDecimal.valueOf(qty));

                    return new OrderDetailVM.ItemVM(
                            safeItemName(oi),
                            oi.getQuantity(),
                            unitPrice,
                            lineTotal,
                            thumb(oi));
                })
                .toList();

        // subtotal = tổng tất cả lineTotal
        BigDecimal subtotal = itemVMs.stream()
                .map(OrderDetailVM.ItemVM::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ====== các field tiền và info từ Order ======
        // Những getter dưới đây bạn cần map đúng với entity Order thực tế của bạn.
        // Nếu Order của bạn CHƯA có các field thì set tạm giá trị mặc định.

        // ví dụ:
        BigDecimal shippingFee = safeBig(order, "getShippingFee", BigDecimal.ZERO);
        BigDecimal discount = safeBig(order, "getDiscountAmount", BigDecimal.ZERO);
        BigDecimal grandTotal = nz(order.getTotalAmount()); // field này bạn đã dùng ở history

        String deliveryAddress = safeInvokeString(order, "getDeliveryAddress"); // địa chỉ giao
        String customerNote = safeInvokeString(order, "getCustomerNote"); // ghi chú
        String paymentMethod = safeInvokeString(order, "getPaymentMethod"); // ví dụ "Tiền mặt", "Momo"
        String branchName = safeBranchName(order); // đã có helper

        // logic có thể hủy: PENDING / CONFIRMED / PREPARING thì true
        boolean cancellable = switch (order.getStatus()) {
            case PENDING, CONFIRMED, PREPARING -> true;
            default -> false;
        };

        return new OrderDetailVM(
                order.getOrderId(), // code
                order.getStatus(), // status
                order.getCreatedAt(), // createdAt
                branchName, // branchName
                deliveryAddress, // deliveryAddress
                customerNote, // customerNote
                paymentMethod, // paymentMethod
                itemVMs, // items
                subtotal, // subtotal
                shippingFee, // shippingFee
                discount, // discount
                grandTotal, // grandTotal
                cancellable // cancellable
        );
    }

    /*
     * =========================================================
     * 3. Helpers chung
     * =========================================================
     */

    private String safeItemName(OrderItem oi) {
        // Ưu tiên snapshot để đảm bảo lịch sử không bị đổi khi item bị sửa/ẩn
        if (notBlank(oi.getItemName())) {
            return oi.getItemName();
        }
        if (oi.getItem() != null && notBlank(safeInvokeString(oi.getItem(), "getName"))) {
            return safeInvokeString(oi.getItem(), "getName");
        }
        return "Món";
    }

    // Xây link thumbnail cho món
    private String thumb(OrderItem oi) {
        if (oi.getItem() == null)
            return PLACEHOLDER_URL;

        // Lấy "khóa" nhận dạng item bằng best-effort: getId / getItemId / getCode /
        // getSlug
        String key = resolveItemKey(oi.getItem());
        if (notBlank(key)) {
            return String.format(ITEM_THUMB_TEMPLATE, key);
        }
        return PLACEHOLDER_URL;
    }

    private String resolveItemKey(Object item) {
        String[] candidates = { "getId", "getItemId", "getCode", "getSlug" };
        for (String m : candidates) {
            String val = safeInvokeString(item, m);
            if (notBlank(val))
                return val;
        }
        return null;
    }

    private String safeBranchName(Order o) {
        if (o.getBranch() == null)
            return "Chi nhánh";
        String name = safeInvokeString(o.getBranch(), "getName");
        return notBlank(name) ? name : "Chi nhánh";
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String safeInvokeString(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object v = m.invoke(target);
            return (v instanceof String s) ? s : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal safeBig(Object target, String methodName, BigDecimal fallback) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object v = m.invoke(target);
            if (v instanceof BigDecimal b) {
                return b;
            }
            return fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public boolean cancelOrderForUser(User user, String code) {
        // tìm đơn
        Order order = orderRepo.findFirstByOrderId(code);
        if (order == null) {
            return false;
        }

        // tự vệ: chỉ chủ đơn mới cancel được
        if (order.getUser() == null || !order.getUser().equals(user)) {
            return false;
        }

        // chỉ cho hủy nếu đang ở trạng thái còn hủy được
        OrderStatus st = order.getStatus();
        boolean cancellable = switch (st) {
            case PENDING, CONFIRMED, PREPARING -> true;
            default -> false;
        };

        if (!cancellable) {
            return false;
        }

        // đổi trạng thái
        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        return true;
    }

}
