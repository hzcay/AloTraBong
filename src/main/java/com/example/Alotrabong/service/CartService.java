package com.example.Alotrabong.service;

import com.example.Alotrabong.dto.AddToCartRequest;
import com.example.Alotrabong.dto.CartItemDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    
    CartItemDTO addToCart(String userId, AddToCartRequest request);
    
    List<CartItemDTO> getCartItems(String userId, String branchId);
    
    CartItemDTO updateCartItem(String userId, String cartItemId, int quantity);
    
    void removeFromCart(String userId, String cartItemId);
    
    void clearCart(String userId, String branchId);
    
    BigDecimal getCartTotal(String userId, String branchId);
    
    int getCartItemCount(String userId, String branchId);
}