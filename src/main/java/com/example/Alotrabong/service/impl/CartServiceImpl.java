package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.AddToCartRequest;
import com.example.Alotrabong.dto.CartItemDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.BadRequestException;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ItemRepository itemRepository;

    @Override
    public CartItemDTO addToCart(String userId, AddToCartRequest request) {
        log.info("Adding item to cart for user: {}, item: {}", userId, request.getItemId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Find or create cart for user and branch
        Cart cart = cartRepository.findByUserAndBranch(user, branch)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .branch(branch)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndItem(cart, item);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem = cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity: {}", cartItem.getCartItemId());
            return convertToDTO(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(request.getQuantity())
                    .unitPrice(item.getPrice())
                    .build();
            
            cartItem = cartItemRepository.save(cartItem);
            log.info("Added new item to cart: {}", cartItem.getCartItemId());
            return convertToDTO(cartItem);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDTO> getCartItems(String userId, String branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Cart cart = cartRepository.findByUserAndBranch(user, branch)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        return cartItemRepository.findByCart(cart).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO updateCartItem(String userId, String cartItemId, int quantity) {
        log.info("Updating cart item: {} to quantity: {}", cartItemId, quantity);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Verify ownership
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to cart item");
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Cart item deleted due to zero quantity: {}", cartItemId);
            return null;
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        log.info("Cart item updated: {}", cartItemId);
        
        return convertToDTO(cartItem);
    }

    @Override
    public void removeFromCart(String userId, String cartItemId) {
        log.info("Removing cart item: {} for user: {}", cartItemId, userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Verify ownership
        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to cart item");
        }
        
        cartItemRepository.delete(cartItem);
        log.info("Cart item removed: {}", cartItemId);
    }

    @Override
    public void clearCart(String userId, String branchId) {
        log.info("Clearing cart for user: {} in branch: {}", userId, branchId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Cart cart = cartRepository.findByUserAndBranch(user, branch)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        cartItemRepository.deleteByCart(cart);
        log.info("Cart cleared for user: {} in branch: {}", userId, branchId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCartTotal(String userId, String branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Cart cart = cartRepository.findByUserAndBranch(user, branch)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        return cartItemRepository.findByCart(cart).stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(String userId, String branchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        
        Cart cart = cartRepository.findByUserAndBranch(user, branch)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        return cartItemRepository.findByCart(cart).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        return CartItemDTO.builder()
                .cartItemId(cartItem.getCartItemId())
                .itemId(cartItem.getItem().getItemId())
                .itemName(cartItem.getItem().getName())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .build();
    }
}
