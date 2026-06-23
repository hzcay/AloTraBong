package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.Order;
import com.example.Alotrabong.entity.PaymentMethod;
import com.example.Alotrabong.entity.PaymentStatus;
import com.example.Alotrabong.repository.OrderRepository;
import com.example.Alotrabong.service.VnpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/vnpay")
public class VnpayCallbackController {

    private final VnpayService vnpayService;
    private final OrderRepository orderRepository;

    @GetMapping("/api/orders/vnpay-return") // 👈 đúng y returnUrl trong log
    @Transactional
    public String vnpReturnAlias(@RequestParam Map<String, String> params, RedirectAttributes ra) {

        boolean valid = vnpayService.validateReturnData(params);
        String status = vnpayService.getPaymentStatus(params); // "SUCCESS"/"FAILED"
        String refRaw = params.getOrDefault("vnp_TxnRef", "");
        String refNoDash = refRaw.replace("-", "");

        log.info("[VNPAY-ALIAS] hit return; valid={}, status={}, ref={}", valid, status, refRaw);

        if (valid && "SUCCESS".equalsIgnoreCase(status)) {
            // thử no-dash trước, rồi fallback raw
            Order order = orderRepository.findByOrderIdNoDash(refNoDash)
                    .or(() -> orderRepository.findByOrderId(refRaw))
                    .orElse(null);

            if (order != null && order.getPaymentStatus() != PaymentStatus.PAID) {
                log.info("[VNPAY-ALIAS] before status={}", order.getPaymentStatus());
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaymentMethod(PaymentMethod.VNPAY);
                orderRepository.saveAndFlush(order); // flush ngay
                log.info("[VNPAY-ALIAS] after status={}", order.getPaymentStatus());
            } else if (order == null) {
                log.warn("[VNPAY-ALIAS] Không tìm thấy order cho {}", refRaw);
            }

            String code = order != null ? order.getOrderId() : refRaw;
            ra.addFlashAttribute("toastSuccess", "Thanh toán thành công cho đơn #" + code);
            return "redirect:/user/checkout/success?code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        }

        ra.addFlashAttribute("toastError", "Thanh toán thất bại hoặc không hợp lệ.");
        return "redirect:/user/checkout/fail";
    }

}
