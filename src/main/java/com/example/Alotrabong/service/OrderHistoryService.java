package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.OrderDetailVM;
import com.example.Alotrabong.dto.OrderHistoryVM;
import com.example.Alotrabong.entity.Address;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.OrderItem;
import com.example.Alotrabong.entity.OrderStatus;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.repository.AddressRepository;
import com.example.Alotrabong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderRepository orderRepo;
    private final AddressRepository addressRepo;

    private static final String PLACEHOLDER_URL = "/images/placeholder.png";
    private static final String ITEM_THUMB_TEMPLATE = "/uploads/items/%s/thumb.jpg";

    // ========== 1) LỊCH SỬ ĐƠN ==========
    public Page<OrderHistoryVM> getHistory(
            User user,
            OrderStatus status,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

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
                .limit(5)
                .map(this::toHistoryItemVM)
                .toList();

        return new OrderHistoryVM(
                o.getOrderId(),
                o.getCreatedAt(),
                branchNameSafe(o),
                o.getStatus(),
                nz(o.getTotalAmount()),
                items
        );
    }

    private OrderHistoryVM.ItemVM toHistoryItemVM(OrderItem oi) {
        BigDecimal unitPrice = nz(oi.getUnitPrice());
        int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;

        BigDecimal subtotal = (oi.getLineTotal() != null)
                ? oi.getLineTotal()
                : unitPrice.multiply(BigDecimal.valueOf(qty));

        return new OrderHistoryVM.ItemVM(
                itemNameSafe(oi),
                qty,
                unitPrice,
                subtotal,
                thumbFromOrderItem(oi)
        );
    }

    // ========== 2) CHI TIẾT 1 ĐƠN ==========
    public OrderDetailVM getOrderDetailForUser(User user, String code) {

        Order order = orderRepo.findFirstByOrderId(code);
        if (order == null) {
            return null;
        }

        if (order.getUser() == null || !order.getUser().equals(user)) {
            return null;
        }

        List<OrderDetailVM.ItemVM> itemVMs = order.getItems().stream()
                .map(oi -> {
                    BigDecimal unitPrice = nz(oi.getUnitPrice());
                    int qty = oi.getQuantity() != null ? oi.getQuantity() : 0;

                    BigDecimal lineTotal = (oi.getLineTotal() != null)
                            ? oi.getLineTotal()
                            : unitPrice.multiply(BigDecimal.valueOf(qty));

                    return new OrderDetailVM.ItemVM(
                            itemNameSafe(oi),
                            oi.getQuantity(),
                            unitPrice,
                            lineTotal,
                            thumbFromOrderItem(oi)
                    );
                })
                .toList();

        BigDecimal subtotal = itemVMs.stream()
                .map(OrderDetailVM.ItemVM::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String branchName = branchNameSafe(order);

        // >>> Đây nè: giờ địa chỉ giao lấy bằng shipping_address -> tra AddressRepo
        String deliveryAddress = resolveDeliveryAddress(order);

        String customerNote = order.getNotes();

        String paymentMethod = (order.getPaymentMethod() != null)
                ? order.getPaymentMethod().name()
                : null;

        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal discount    = BigDecimal.ZERO;
        BigDecimal grandTotal  = nz(order.getTotalAmount());

        boolean cancellable = switch (order.getStatus()) {
            case PENDING, CONFIRMED, PREPARING -> true;
            default -> false;
        };

        return new OrderDetailVM(
                order.getOrderId(),
                order.getStatus(),
                order.getCreatedAt(),
                branchName,
                deliveryAddress,
                customerNote,
                paymentMethod,
                itemVMs,
                subtotal,
                shippingFee,
                discount,
                grandTotal,
                cancellable
        );
    }

    // ========== 3) HELPERS ==========

    private String resolveDeliveryAddress(Order order) {
        // Lấy cái đang lưu trong orders.shipping_address
        // Ở flow hiện tại: shipping_address = addressId user chọn (ví dụ "1")
        String raw = order.getShippingAddress();

        if (raw == null || raw.isBlank()) {
            return null;
        }

        // raw có thể là "1"
        try {
            Integer addrId = Integer.valueOf(raw.trim());

            // tìm Address theo id đó
            Address ad = addressRepo.findById(addrId).orElse(null);
            if (ad != null) {
                return formatAddress(ad);
            }

            // nếu không tìm thấy address thì fallback in thẳng "1"
            return raw.trim();

        } catch (NumberFormatException ex) {
            // raw KHÔNG phải số (ví dụ sau này mày thay đổi và nhét full text vào luôn)
            return raw.trim();
        }
    }

    // format Address -> "Tên (SĐT) - Số nhà, Quận, TP"
    private String formatAddress(Address ad) {
        if (ad == null) return null;

        StringBuilder sb = new StringBuilder();

        // Người nhận + sđt
        if (notBlank(ad.getReceiverName())) {
            sb.append(ad.getReceiverName().trim());
        }
        if (notBlank(ad.getPhone())) {
            if (!sb.isEmpty()) {
                sb.append(" (").append(ad.getPhone().trim()).append(")");
            } else {
                sb.append(ad.getPhone().trim());
            }
        }

        // chi tiết địa chỉ
        boolean hasDetail = false;
        if (notBlank(ad.getAddressLine())) {
            if (!sb.isEmpty()) sb.append(" - ");
            sb.append(ad.getAddressLine().trim());
            hasDetail = true;
        }
        if (notBlank(ad.getDistrict())) {
            sb.append(hasDetail ? ", " : " - ");
            sb.append(ad.getDistrict().trim());
            hasDetail = true;
        }
        if (notBlank(ad.getCity())) {
            sb.append(hasDetail ? ", " : " - ");
            sb.append(ad.getCity().trim());
        }

        String out = sb.toString().trim();
        return out.isBlank() ? null : out;
    }

    private String itemNameSafe(OrderItem oi) {
        if (oi.getItemName() != null && !oi.getItemName().isBlank()) {
            return oi.getItemName();
        }
        if (oi.getItem() != null
                && oi.getItem().getName() != null
                && !oi.getItem().getName().isBlank()) {
            return oi.getItem().getName();
        }
        return "Món";
    }

    private String thumbFromOrderItem(OrderItem oi) {
        if (oi.getItem() != null && oi.getItem().getItemId() != null) {
            String key = oi.getItem().getItemId().toString();
            if (!key.isBlank()) {
                return String.format(ITEM_THUMB_TEMPLATE, key);
            }
        }
        return PLACEHOLDER_URL;
    }

    private String branchNameSafe(Order o) {
        if (o.getBranch() == null) return "Chi nhánh";
        String n = o.getBranch().getName();
        return (n == null || n.isBlank()) ? "Chi nhánh" : n;
    }

    private BigDecimal nz(BigDecimal v) {
        return (v != null) ? v : BigDecimal.ZERO;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    // ========== 4) HỦY ĐƠN ==========
    public boolean cancelOrderForUser(User user, String code) {
        Order order = orderRepo.findFirstByOrderId(code);
        if (order == null) {
            return false;
        }

        if (order.getUser() == null || !order.getUser().equals(user)) {
            return false;
        }

        OrderStatus st = order.getStatus();
        boolean cancellable = switch (st) {
            case PENDING, CONFIRMED, PREPARING -> true;
            default -> false;
        };

        if (!cancellable) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        return true;
    }
}
