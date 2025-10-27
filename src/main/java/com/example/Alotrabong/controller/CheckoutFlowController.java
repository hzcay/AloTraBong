package com.example.Alotrabong.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/checkout")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CheckoutFlowController {

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
    public String confirmOrder(@RequestParam(required = false) String addressId,
                               @RequestParam(required = false) String payment,
                               @RequestParam(required = false) String note,
                               @RequestParam(value = "branchId", required = false, defaultValue = "b1") String branchId,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (addressId == null || addressId.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn địa chỉ giao hàng.");
            return "redirect:/user/checkout?branchId=" + branchId;
        }
        if (payment == null || payment.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn phương thức thanh toán.");
            return "redirect:/user/checkout?branchId=" + branchId;
        }

        // TODO: thực hiện tạo Order thật sự + clear giỏ
        session.removeAttribute("selectedAddressId");
        session.removeAttribute("selectedPayment");
        session.removeAttribute("checkoutNote");
        session.removeAttribute("appliedCouponCode");
        session.removeAttribute("couponError");

        ra.addFlashAttribute("toastSuccess", "Đặt hàng thành công!");
        // Có thể redirect sang trang chi tiết đơn theo code sinh ra
        return "redirect:/user/order/history";
    }
}
