package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.AddToCartRequest;
import com.example.Alotrabong.dto.ApiResponse;
import com.example.Alotrabong.dto.CartItemDTO;
import com.example.Alotrabong.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{branchId}")
    @Operation(summary = "Get cart items for specific branch")
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(
            @PathVariable String branchId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        List<CartItemDTO> items = cartService.getCartItems(userId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Cart items retrieved", items));
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        CartItemDTO item = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", item));
    }

    @PutMapping("/update/{cartItemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @PathVariable String cartItemId,
            @RequestParam int quantity,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        CartItemDTO item = cartService.updateCartItem(userId, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", item));
    }

    @DeleteMapping("/remove/{cartItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable String cartItemId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }

    @DeleteMapping("/clear/{branchId}")
    @Operation(summary = "Clear cart for specific branch")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @PathVariable String branchId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        cartService.clearCart(userId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    @GetMapping("/total/{branchId}")
    @Operation(summary = "Get cart total for specific branch")
    public ResponseEntity<ApiResponse<BigDecimal>> getCartTotal(
            @PathVariable String branchId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        BigDecimal total = cartService.getCartTotal(userId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Cart total calculated", total));
    }

    @GetMapping("/count/{branchId}")
    @Operation(summary = "Get cart item count for specific branch")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
            @PathVariable String branchId,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        int count = cartService.getCartItemCount(userId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Cart item count", count));
    }

    private String getUserIdFromAuth(Authentication authentication) {
        // TODO: Implement based on your JWT authentication setup
        return "user-id-placeholder"; // Placeholder
    }
}
