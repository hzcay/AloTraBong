package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.*;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/user/checkout")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class CheckoutFlowController {

    private final OrderService orderService;
    private final VnpayService vnpayService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BranchRepository branchRepository;
    private final CartService cartService;
    private final ItemMediaRepository itemMediaRepository;
    private final AddressService addressService;
    private final CouponService couponService;

    // =======================
    // CHỌN ĐỊA CHỈ GIAO HÀNG
    // =======================
    @PostMapping("/address")
    public String selectAddress(@RequestParam String addressId,
            @RequestParam(value = "branchId", required = false) String branchId,
            HttpSession session,
            RedirectAttributes ra) {
        session.setAttribute("selectedAddressId", addressId);
        ra.addFlashAttribute("toastSuccess", "Đã chọn địa chỉ giao hàng.");
        return "redirect:/user/checkout?branchId=" + (branchId != null ? branchId : "");
    }

    // =======================
    // CHỌN PHƯƠNG THỨC THANH TOÁN
    // =======================
    @PostMapping("/payment")
    public String selectPayment(@RequestParam String payment,
            @RequestParam(required = false) String note,
            @RequestParam(value = "branchId", required = false) String branchId,
            HttpSession session,
            RedirectAttributes ra) {
        session.setAttribute("selectedPayment", payment);
        session.setAttribute("checkoutNote", note);
        ra.addFlashAttribute("toastSuccess", "Đã chọn phương thức thanh toán.");
        return "redirect:/user/checkout?branchId=" + (branchId != null ? branchId : "");
    }

    // =======================
    // ÁP DỤNG MÃ GIẢM GIÁ
    // =======================
    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam String code,
            @RequestParam(value = "branchId", required = false) String branchId,
            Authentication auth,
            HttpSession session,
            RedirectAttributes ra) {
        String login = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        if (login == null) {
            ra.addFlashAttribute("toastError", "Bạn cần đăng nhập để dùng mã.");
            return "redirect:/login";
        }

        String effectiveBranchId = (branchId != null && !branchId.isBlank())
                ? branchId
                : (String) session.getAttribute("selectedBranchId");

        // Đọc subtotal typed từ session
        Object attr = session.getAttribute("lastCheckoutSummary");
        BigDecimal subtotal = (attr instanceof CheckoutSummary cs) ? cs.subtotal() : BigDecimal.ZERO;

        CouponValidationResult vr = couponService.validateAndPreview(login, effectiveBranchId, code, subtotal);

        if (vr.ok()) {
            session.setAttribute("appliedCouponCode", code);
            session.setAttribute("appliedCouponDiscount", vr.previewDiscount());
            session.removeAttribute("couponError");
            ra.addFlashAttribute("toastSuccess", vr.message());
        } else {
            session.removeAttribute("appliedCouponCode");
            session.removeAttribute("appliedCouponDiscount");
            session.setAttribute("couponError", vr.message());
            ra.addFlashAttribute("toastError", vr.message());
        }
        return "redirect:/user/checkout?branchId=" + (effectiveBranchId != null ? effectiveBranchId : "");
    }

    // =======================
    // XÁC NHẬN ĐẶT HÀNG
    // =======================
    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam String addressId,
            @RequestParam String payment,
            @RequestParam(required = false) String note,
            @RequestParam(value = "branchId", required = false) String branchId,
            Authentication auth,
            HttpSession session,
            RedirectAttributes ra) {

        // ==== Validate basic inputs
        if (addressId.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn địa chỉ giao hàng.");
            return "redirect:/user/checkout?branchId=" + (branchId != null ? branchId : "");
        }
        if (payment.isBlank()) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn phương thức thanh toán.");
            return "redirect:/user/checkout?branchId=" + (branchId != null ? branchId : "");
        }

        // ==== Auth
        String login = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        if (login == null) {
            ra.addFlashAttribute("toastError", "Phiên đăng nhập không hợp lệ.");
            return "redirect:/login";
        }

        // ==== Resolve user & branch
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String userId = user.getUserId();

        String effectiveBranchId = (branchId != null && !branchId.isBlank())
                ? branchId
                : (String) session.getAttribute("selectedBranchId");

        if (effectiveBranchId == null) {
            ra.addFlashAttribute("toastError", "Vui lòng chọn chi nhánh phục vụ.");
            return "redirect:/user/checkout";
        }

        // ==== Create Order (chỉ tạo dòng & items/subtotal — KHÔNG gộp ship/discount ở
        // đây)
        CreateOrderRequest req = CreateOrderRequest.builder()
                .branchId(effectiveBranchId)
                .shippingAddress(addressId)
                .notes(note)
                .build();

        OrderDTO dto = orderService.createOrder(userId, req);

        // ==== Map payment method
        PaymentMethod pm = switch (payment.toUpperCase()) {
            case "VNPAY" -> PaymentMethod.VNPAY;
            case "MOMO" -> PaymentMethod.MOMO;
            default -> PaymentMethod.COD;
        };

        // ==== Load order vừa tạo
        Order persisted = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Set trạng thái thanh toán & method
        persisted.setPaymentMethod(pm);
        if (persisted.getPaymentStatus() == null) {
            persisted.setPaymentStatus(PaymentStatus.UNPAID);
        }

        // ==== Lấy summary từ session (nguồn chân lý UI)
        CheckoutSummary sum = (CheckoutSummary) session.getAttribute("lastCheckoutSummary");

        // Shipping fee (ưu tiên từ summary; fallback = 15000)
        BigDecimal shippingFee = (sum != null && sum.shippingFee() != null)
                ? sum.shippingFee()
                : new BigDecimal("15000");

        // Discount (ưu tiên giá trị đã preview apply-coupon; fallback = sum.discount;
        // else 0)
        BigDecimal discount = (BigDecimal) session.getAttribute("appliedCouponDiscount");
        if (discount == null) {
            discount = (sum != null && sum.discount() != null) ? sum.discount() : BigDecimal.ZERO;
        }
        if (discount.signum() < 0) { // sanity
            discount = BigDecimal.ZERO;
        }

        // ==== Tính tổng cuối cùng — KHÔNG cộng chồng từ persisted.getTotalAmount()
        BigDecimal totalAmount;
        if (sum != null && sum.grandTotal() != null) {
            // Nếu đã có grand trong summary thì dùng luôn cho khớp UI
            totalAmount = sum.grandTotal();
        } else if (sum != null && sum.subtotal() != null) {
            // Tự tính lại từ subtotal + ship - discount
            totalAmount = sum.subtotal().add(shippingFee).subtract(discount);
        } else {
            // Fallback "an toàn" nếu thiếu summary: chỉ lấy ship - discount (tránh
            // double-count).
            // Khuyến nghị: đảm bảo createOrder KHÔNG set totalAmount trước confirm.
            BigDecimal base = BigDecimal.ZERO;
            totalAmount = base.add(shippingFee).subtract(discount);
        }
        if (totalAmount.signum() < 0)
            totalAmount = BigDecimal.ZERO;

        // ==== Ghi xuống DB: ship/discount/total
        persisted.setShippingFee(shippingFee);
        persisted.setDiscount(discount);
        persisted.setTotalAmount(totalAmount);
        orderRepository.save(persisted);

        // ==== Nếu VNPAY thì tạo link và redirect
        if (pm == PaymentMethod.VNPAY) {
            try {
                String url = vnpayService.createPaymentUrl(persisted);
                return "redirect:" + url;
            } catch (Exception e) {
                ra.addFlashAttribute("toastError", "Không tạo được link thanh toán VNPAY.");
                return "redirect:/user/checkout";
            }
        }

        // ==== Non-gateway: clear session & done
        clearCheckoutSession(session);
        ra.addFlashAttribute("toastSuccess", "Đặt hàng thành công!");
        return "redirect:/user/order/history";
    }

    private void clearCheckoutSession(HttpSession session) {
        session.removeAttribute("selectedAddressId");
        session.removeAttribute("selectedPayment");
        session.removeAttribute("checkoutNote");
        session.removeAttribute("appliedCouponCode");
        session.removeAttribute("appliedCouponDiscount");
        session.removeAttribute("couponError");
        session.removeAttribute("lastCheckoutSummary");
    }

    // =======================
    // TRANG THANH TOÁN
    // =======================
    @GetMapping
    public String showCheckoutPage(@RequestParam(required = false) String branchIdParam,
            Authentication auth,
            HttpSession session,
            Model model) {

        String userLogin = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;

        if (branchIdParam != null && !branchIdParam.isBlank()) {
            session.setAttribute("selectedBranchId", branchIdParam);
        }

        String selectedBranchId = (String) session.getAttribute("selectedBranchId");
        if (selectedBranchId == null || selectedBranchId.isBlank()) {
            Branch defaultBranch = branchRepository.findFirstByIsActiveTrueOrderByCreatedAtAsc().orElse(null);
            selectedBranchId = (defaultBranch != null ? defaultBranch.getBranchId() : null);
            session.setAttribute("selectedBranchId", selectedBranchId);
        }

        List<Branch> branches = branchRepository.findByIsActiveTrue();
        String selectedAddressId = (String) session.getAttribute("selectedAddressId");
        Address selectedAddress = null;

        if (selectedAddressId != null && !selectedAddressId.isBlank()) {
            try {
                Integer addrId = Integer.valueOf(selectedAddressId);
                selectedAddress = addressRepository.findById(addrId).orElse(null);
            } catch (NumberFormatException ignore) {
            }
        }

        if (selectedAddress != null && selectedAddress.getCity() != null) {
            String city = selectedAddress.getCity().trim().toLowerCase();

            if (city.contains("hồ chí minh") || city.contains("ho chi minh") || city.contains("hcm")) {
                branches = branchRepository.findByCityAndIsActiveTrue("Ho Chi Minh");
            } else if (city.contains("hà nội") || city.contains("ha noi")) {
                branches = branchRepository.findByCityAndIsActiveTrue("Ha Noi");
            } else {
                model.addAttribute("branchError", "Hiện chỉ hỗ trợ giao hàng tại TP.HCM và Hà Nội.");
                branches = List.of();
            }
        }

        model.addAttribute("branches", branches);
        model.addAttribute("branchId", selectedBranchId);

        var cartItems = cartService.getCartItems(
                userLogin != null ? userLogin : "guest",
                selectedBranchId);

        var items = new ArrayList<Map<String, Object>>();
        for (var d : cartItems) {
            var it = new HashMap<String, Object>();
            String thumb = itemMediaRepository
                    .findByItem_ItemIdOrderBySortOrderAsc(d.getItemId())
                    .stream()
                    .findFirst()
                    .map(ItemMedia::getMediaUrl)
                    .orElse("/img/products/" + d.getItemId() + ".jpg");
            it.put("name", d.getItemName());
            it.put("thumbnailUrl", thumb);
            it.put("unitPrice", d.getUnitPrice());
            it.put("qty", d.getQuantity());
            it.put("subtotal", d.getTotalPrice());
            items.add(it);
        }

        BigDecimal subtotal = cartItems.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = new BigDecimal("15000");

        // Danh sách mã đủ điều kiện
        List<CouponLiteDTO> eligible = couponService.findEligible(userLogin, selectedBranchId, subtotal);
        model.addAttribute("eligibleCoupons", eligible);

        // Áp mã nếu còn hợp lệ
        String appliedCode = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = BigDecimal.ZERO;
        if (appliedCode != null) {
            CouponValidationResult vr = couponService.validateAndPreview(userLogin, selectedBranchId, appliedCode,
                    subtotal);
            if (vr.ok()) {
                discount = vr.previewDiscount();
            } else {
                session.removeAttribute("appliedCouponCode");
                session.removeAttribute("appliedCouponDiscount");
                model.addAttribute("couponError", vr.message());
            }
        }

        BigDecimal grand = subtotal.add(shipping).subtract(discount);

        model.addAttribute("items", items);

        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("subtotal", subtotal);
        summaryMap.put("discount", discount);
        summaryMap.put("shippingFee", shipping);
        summaryMap.put("grandTotal", grand);
        model.addAttribute("summary", summaryMap);

        // Lưu bản typed vào session cho /apply-coupon đọc
        CheckoutSummary summary = new CheckoutSummary(subtotal, discount, shipping, grand);
        session.setAttribute("lastCheckoutSummary", summary);

        List<AddressDTO> addresses = (userLogin != null)
                ? addressService.getAddressesForUser(userLogin)
                : List.of();
        model.addAttribute("addresses", addresses);

        model.addAttribute("selectedAddressId", session.getAttribute("selectedAddressId"));
        model.addAttribute("selectedPayment", session.getAttribute("selectedPayment"));
        model.addAttribute("note", session.getAttribute("checkoutNote"));
        model.addAttribute("couponError", session.getAttribute("couponError"));
        model.addAttribute("appliedCoupon",
                appliedCode != null ? Map.of("code", appliedCode) : null);

        return "user/checkout/checkout";
    }
}
