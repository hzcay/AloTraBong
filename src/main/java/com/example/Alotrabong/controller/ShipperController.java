package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.entity.Shipper;
import com.example.Alotrabong.service.ShipperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shippers")
@RequiredArgsConstructor
@Tag(name = "Shipper", description = "Shipper management APIs")
public class ShipperController {

    private final ShipperService shipperService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get all shippers")
    public ResponseEntity<ApiResponse<List<Shipper>>> getAllShippers() {
        List<Shipper> shippers = shipperService.getAllShippers();
        return ResponseEntity.ok(ApiResponse.success("Shippers retrieved", shippers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
    @Operation(summary = "Get shipper by ID")
    public ResponseEntity<ApiResponse<Shipper>> getShipperById(@PathVariable String id) {
        Shipper shipper = shipperService.getShipperById(id);
        return ResponseEntity.ok(ApiResponse.success("Shipper retrieved", shipper));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create shipper")
    public ResponseEntity<ApiResponse<Shipper>> createShipper(@RequestBody Shipper shipper) {
        Shipper created = shipperService.createShipper(shipper);
        return ResponseEntity.ok(ApiResponse.success("Shipper created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update shipper")
    public ResponseEntity<ApiResponse<Shipper>> updateShipper(
            @PathVariable String id,
            @RequestBody Shipper shipper) {
        Shipper updated = shipperService.updateShipper(id, shipper);
        return ResponseEntity.ok(ApiResponse.success("Shipper updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete shipper")
    public ResponseEntity<ApiResponse<Void>> deleteShipper(@PathVariable String id) {
        shipperService.deleteShipper(id);
        return ResponseEntity.ok(ApiResponse.success("Shipper deleted successfully", null));
    }
}