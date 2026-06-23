package com.example.Alotrabong.controller;

import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.entity.Shipment;
import com.example.Alotrabong.repository.ShipperRepository;
import com.example.Alotrabong.service.ShipperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/shipper")
@PreAuthorize("hasAnyRole('ADMIN', 'SHIPPER')")
@RequiredArgsConstructor
@Slf4j
public class ShipperController {

    private final ShipperRepository shipperRepository;
    private final ShipperService shipperService;

    /**
     * Mặc định vào /shipper → redirect sang deliveries
     */
    @GetMapping({ "", "/" })
    public String redirectToDeliveries() {
        return "redirect:/shipper/deliveries";
    }

    /**
     * Hiển thị danh sách shipment được phân công
     */
    @GetMapping("/deliveries")
    public String deliveries(
            Authentication auth,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        String email = auth.getName();
        Shipper shipper = shipperRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin shipper cho email: " + email));

        List<Shipment> shipments = shipperService.filterShipments(shipper.getShipperId(), status, fromDate, toDate);

        // ✅ Thống kê
        long totalReceived = shipperService.countPickedUp(shipper.getShipperId());
        long totalDelivered = shipperService.countDelivered(shipper.getShipperId());
        BigDecimal totalMoney = shipperService.totalDeliveredAmount(shipper.getShipperId());

        model.addAttribute("shipments", shipments);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("totalReceived", totalReceived);
        model.addAttribute("totalDelivered", totalDelivered);
        model.addAttribute("totalMoney", totalMoney);

        return "shipper/shipments/list";
    }

    /**
     * Xác nhận nhận đơn giao hàng
     */
    @PostMapping("/shipments/{id}/confirm")
    public String confirmShipment(@PathVariable("id") String shipmentId,
            RedirectAttributes ra) {
        shipperService.confirmPickup(shipmentId);
        ra.addFlashAttribute("msg", "✅ Đã nhận đơn giao hàng thành công!");
        return "redirect:/shipper/deliveries";
    }

    /**
     * Xác nhận giao hàng hoàn tất
     */
    @PostMapping("/shipments/{id}/confirm-delivered")
    public String confirmShipmentDelivered(@PathVariable("id") String shipmentId,
            RedirectAttributes ra) {
        shipperService.confirmDelivered(shipmentId);
        ra.addFlashAttribute("msg", "✅ Đã giao đơn giao hàng thành công!");
        return "redirect:/shipper/deliveries";
    }

    /**
     * ✅ Upload ảnh check-in (tạm thời lưu local)
     */
    @PostMapping("/shipments/{id}/checkin")
    @ResponseBody
    public ResponseEntity<?> uploadCheckinPhoto(
            @PathVariable("id") String shipmentId,
            @RequestParam("photo") MultipartFile photo) {

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ảnh check-in"));
        }

        try {
            // Tạo thư mục lưu nếu chưa có
            Path uploadDir = Paths.get("uploads/checkins/");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Tạo tên file duy nhất
            String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);

            // Ghi file vào thư mục uploads/checkins
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL truy cập ảnh (tạm /uploads/checkins/...)
            String url = "/uploads/checkins/" + filename;

            // Lưu đường dẫn ảnh vào shipment
            shipperService.saveCheckinPhoto(shipmentId, url);

            log.info("✅ Check-in ảnh thành công cho shipment {} -> {}", shipmentId, url);
            return ResponseEntity.ok(Map.of("success", true, "url", url));

        } catch (IOException e) {
            log.error("❌ Lỗi upload ảnh check-in", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Lịch sử giao hàng
     */
    @GetMapping("/history")
    public String deliveryHistory(Model model) {
        model.addAttribute("title", "Delivery History");
        return "shipper/history";
    }
}
