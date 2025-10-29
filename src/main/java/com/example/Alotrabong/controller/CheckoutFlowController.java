package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.CreateOrderRequest;
import com.example.Alotrabong.dto.OrderDTO;
import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.PaymentMethod;
import com.example.Alotrabong.entity.PaymentStatus;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.OrderRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.service.OrderService;
import com.example.Alotrabong.service.VnpayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/checkout")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CheckoutFlowController {

    private final OrderService orderService;
    private final VnpayService vnpayService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/address")
    public String selectAddress(@RequestParam String addressId,
            @RequestParam(value = "branchId", required = false, defaultValue = "b1") String branchId,
            HttpSession session,
            RedirectAttributes ra) {
        session.setAttribute("selectedAddressId", addressId);
        ra.addFlashAttribute("toastSuccess", "Đã chọn địa chỉ giao hàng.");
        return "redirect:/user/checkout?branchId=" + branchId;
    }

    @PostMapping("/payment")
    public String selectPayment(@RequestParam String payment,
            @RequestParam(required = false) String note,
            @RequestParam(value = "branchId", required = false, defaultValue = "b1") String branchId,
            HttpSession session,
            RedirectAttributes ra) {
        session.setAttribute("selectedPayment", payment);
        session.setAttribute("checkoutNote", note);
        ra.addFlashAttribute("toastSuccess", "Đã chọn phương thức thanh toán.");
        return "redirect:/user/checkout?branchId=" + branchId;
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String code,
            @RequestParam(value = "branchId", required = false, defaultValue = "b1") String branchId,
            HttpSession session,
            RedirectAttributes ra) {
        if ("ALO20".equalsIgnoreCase(code)) {
            session.setAttribute("appliedCouponCode", code);
            session.removeAttribute("couponError");
            ra.addFlashAttribute("toastSuccess", "Áp dụng mã ALO20 thành công (-20.000đ).");
        } else {
            session.removeAttribute("appliedCouponCode");
            session.setAttribute("couponError", "Mã không hợp lệ hoặc đã hết hạn.");
            ra.addFlashAttribute("toastError", "Mã giảm giá không hợp lệ.");
        }
        return "redirect:/user/checkout?branchId=" + branchId;
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam String addressId,
            @RequestParam String payment,
            @RequestParam(required = false) String note,
            @RequestParam(value = "branchId", required = false) String branchId,
            Authentication auth,
            HttpSession session,
            RedirectAttributes ra) {
        if (addressId.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn địa chỉ giao hàng.");
            return "redirect:/user/checkout?branchId=" + branchId;
        }
        if (payment.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn phương thức thanh toán.");
            return "redirect:/user/checkout?branchId=" + branchId;
        }

        // Map login (email/phone) -> User -> userId (PK)
        String login = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        if (login == null) {
            ra.addFlashAttribute("toastError", "Phiên đăng nhập không hợp lệ.");
            return "redirect:/login";
        }
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String userId = user.getUserId();

        // Tạo order (DTO không cần payment)
        CreateOrderRequest req = CreateOrderRequest.builder()
                .branchId(branchId)
                .shippingAddress(addressId) // nếu đây là id, tầng service map sang text full theo thiết kế của bạn
                .notes(note)
                .build();

        OrderDTO dto = orderService.createOrder(userId, req);

        // --- set paymentMethod tối thiểu để không-null ---
        PaymentMethod pm = "VNPAY".equalsIgnoreCase(payment) ? PaymentMethod.VNPAY
                : "MOMO".equalsIgnoreCase(payment) ? PaymentMethod.MOMO
                        : PaymentMethod.COD;

        Order persisted = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        persisted.setPaymentMethod(pm);
        if (persisted.getPaymentStatus() == null) {
            persisted.setPaymentStatus(PaymentStatus.UNPAID);
        }
        orderRepository.save(persisted);

        // VNPAY → build URL và redirect thẳng
        if (pm == PaymentMethod.VNPAY) {
            try {
                String url = vnpayService.createPaymentUrl(persisted);
                return "redirect:" + url;
            } catch (Exception e) {
                ra.addFlashAttribute("toastError", "Không tạo được link thanh toán VNPAY.");
                return "redirect:/user/checkout?branchId=" + branchId;
            }
        }

        // COD/MOMO → quay lịch sử đơn (tuỳ bạn xử lý thêm)
        clearCheckoutSession(session);
        ra.addFlashAttribute("toastSuccess", "Đặt hàng thành công!");
        return "redirect:/user/order/history";
    }

    private void clearCheckoutSession(HttpSession session) {
        session.removeAttribute("selectedAddressId");
        session.removeAttribute("selectedPayment");
        session.removeAttribute("checkoutNote");
        session.removeAttribute("appliedCouponCode");
        session.removeAttribute("couponError");
    }
}