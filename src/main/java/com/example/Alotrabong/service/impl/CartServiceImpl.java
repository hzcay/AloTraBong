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
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

        private static final int MAX_QTY = 99;

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final ItemRepository itemRepository;
        private final BranchItemPriceRepository branchItemPriceRepository;
        private final InventoryRepository inventoryRepository;

        // =====================================================
        // PUBLIC METHODS
        // =====================================================

        @Override
        public CartItemDTO addToCart(String userIdOrLogin, AddToCartRequest request) {
                log.info("Adding item to cart | user={}, item={}, branch={}, qty={}",
                                userIdOrLogin, request.getItemId(), request.getBranchId(), request.getQuantity());

                int wantQty = (request.getQuantity() == null || request.getQuantity() <= 0) ? 1 : request.getQuantity();
                wantQty = Math.min(wantQty, MAX_QTY);

                User user = resolveUser(userIdOrLogin);
                Item item = itemRepository.findById(request.getItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                Branch branch = resolveBranch(request.getBranchId()); // chỉ để check giá/tồn kho

                if (Boolean.FALSE.equals(item.getIsActive())) {
                        throw new BadRequestException("Item is inactive");
                }

                PriceAvail pa = resolvePriceAndAvailability(item, branch);
                if (!pa.available) {
                        throw new BadRequestException("Item is not available at this branch");
                }

                if (!hasSufficientInventory(branch.getBranchId(), item.getItemId(), wantQty)) {
                        throw new BadRequestException("Insufficient inventory");
                }

                Cart cart = getOrCreateCart(user); // ❗️dùng chung giỏ hàng, không phân branch

                CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item)
                                .orElseGet(() -> cartItemRepository.save(
                                                CartItem.builder()
                                                                .cart(cart)
                                                                .item(item)
                                                                .quantity(0)
                                                                .unitPrice(BigDecimal.ZERO)
                                                                .build()));

                BigDecimal baseUnit = pa.price != null ? pa.price
                                : (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                BigDecimal optionExtras = calcOptionExtras(cartItem);
                BigDecimal effectiveUnit = baseUnit.add(optionExtras).setScale(2, RoundingMode.HALF_UP);

                int newQty = Math.min(
                                (cartItem.getQuantity() == null ? 0 : cartItem.getQuantity()) + wantQty,
                                MAX_QTY);
                cartItem.setQuantity(newQty);
                cartItem.setUnitPrice(effectiveUnit);
                cartItem = cartItemRepository.save(cartItem);

                log.info("Added/merged cartItem={} qty={} unit={}", cartItem.getCartItemId(), newQty, effectiveUnit);
                return convertToDTO(cartItem);
        }

        @Override
        @Transactional(readOnly = true)
        public List<CartItemDTO> getCartItems(String userIdOrLogin, String branchIdOrCode) {
                User user = resolveUser(userIdOrLogin);

                // ❗️dùng chung giỏ hàng, không lọc branch
                Optional<Cart> opt = cartRepository.findByUser(user);
                if (opt.isEmpty())
                        return List.of();

                Cart cart = opt.get();
                return cartItemRepository.findByCart(cart).stream()
                                .map(this::convertToDTO)
                                .collect(toList());
        }

        @Override
        public CartItemDTO updateCartItem(String userIdOrLogin, String cartItemId, int quantity) {
                log.info("Updating cart item | user={}, cartItemId={}, qty={}", userIdOrLogin, cartItemId, quantity);

                CartItem cartItem = cartItemRepository.findById(cartItemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

                ensureOwnership(userIdOrLogin, cartItem.getCart());

                if (quantity <= 0) {
                        cartItemRepository.delete(cartItem);
                        log.info("Removed cart item due to qty<=0 | {}", cartItemId);
                        return null;
                }

                cartItem.setQuantity(Math.min(quantity, MAX_QTY));
                Branch defaultBranch = branchRepository.findFirstByIsActiveTrueOrderByCreatedAtAsc().orElse(null);
                PriceAvail pa = resolvePriceAndAvailability(cartItem.getItem(), defaultBranch);

                BigDecimal base = pa.price != null ? pa.price
                                : (cartItem.getItem().getPrice() != null ? cartItem.getItem().getPrice()
                                                : BigDecimal.ZERO);
                BigDecimal extra = calcOptionExtras(cartItem);
                cartItem.setUnitPrice(base.add(extra).setScale(2, RoundingMode.HALF_UP));

                cartItem = cartItemRepository.save(cartItem);
                log.info("Updated cart item ok | {}", cartItemId);
                return convertToDTO(cartItem);
        }

        @Override
        public void removeFromCart(String userIdOrLogin, String cartItemId) {
                CartItem cartItem = cartItemRepository.findById(cartItemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
                ensureOwnership(userIdOrLogin, cartItem.getCart());
                cartItemRepository.delete(cartItem);
        }

        @Override
        public void clearCart(String userIdOrLogin, String branchIdOrCode) {
                User user = resolveUser(userIdOrLogin);
                Optional<Cart> opt = cartRepository.findByUser(user);
                if (opt.isEmpty())
                        return;
                cartItemRepository.deleteByCart(opt.get());
        }

        @Override
        @Transactional(readOnly = true)
        public BigDecimal getCartTotal(String userIdOrLogin, String branchIdOrCode) {
                return getCartItems(userIdOrLogin, branchIdOrCode).stream()
                                .map(CartItemDTO::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        @Transactional(readOnly = true)
        public int getCartItemCount(String userIdOrLogin, String branchIdOrCode) {
                return getCartItems(userIdOrLogin, branchIdOrCode).stream()
                                .mapToInt(CartItemDTO::getQuantity)
                                .sum();
        }

        // =====================================================
        // PRIVATE HELPERS
        // =====================================================

        private User resolveUser(String userIdOrLogin) {
                if (userIdOrLogin == null || userIdOrLogin.isBlank()) {
                        throw new ResourceNotFoundException("User not found");
                }
                return userRepository.findById(userIdOrLogin)
                                .or(() -> userRepository.findByLogin(userIdOrLogin))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        private Branch resolveBranch(String idOrCode) {
                if (idOrCode != null && !idOrCode.isBlank()) {
                        return branchRepository.findById(idOrCode)
                                        .or(() -> branchRepository.findByBranchCodeIgnoreCase(idOrCode))
                                        .orElseGet(() -> branchRepository.findFirstByIsActiveTrueOrderByCreatedAtAsc()
                                                        .orElse(null));
                }
                return branchRepository.findFirstByIsActiveTrueOrderByCreatedAtAsc().orElse(null);
        }

        private Cart getOrCreateCart(User user) {
                return cartRepository.findByUser(user)
                                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        }

        private void ensureOwnership(String userIdOrLogin, Cart cart) {
                if (cart == null || cart.getUser() == null)
                        throw new BadRequestException("Unauthorized access to cart");
                User current = resolveUser(userIdOrLogin);
                if (!Objects.equals(cart.getUser().getUserId(), current.getUserId()))
                        throw new BadRequestException("Unauthorized access to cart");
        }

        private PriceAvail resolvePriceAndAvailability(Item item, Branch branch) {
                BigDecimal price = item.getPrice();
                boolean available = Boolean.TRUE.equals(item.getIsActive());

                if (branch != null) {
                        var bipOpt = branchItemPriceRepository.findByItemAndBranch(item, branch);
                        if (bipOpt.isPresent()) {
                                BranchItemPrice bip = bipOpt.get();
                                if (bip.getPrice() != null)
                                        price = bip.getPrice();
                                if (bip.getIsAvailable() != null)
                                        available = available && bip.getIsAvailable();
                        }
                }
                return new PriceAvail(price, available);
        }

        private boolean hasSufficientInventory(String branchId, String itemId, int wantQty) {
                return inventoryRepository.findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                                .map(inv -> inv.getQuantity() != null && inv.getQuantity() >= wantQty)
                                .orElse(true);
        }

        private BigDecimal calcOptionExtras(CartItem ci) {
                if (ci.getOptions() == null || ci.getOptions().isEmpty())
                        return BigDecimal.ZERO;
                return ci.getOptions().stream()
                                .map(o -> o.getExtraPrice() != null ? o.getExtraPrice() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private CartItemDTO convertToDTO(CartItem ci) {
                BigDecimal unit = ci.getUnitPrice() != null ? ci.getUnitPrice() : BigDecimal.ZERO;
                int qty = ci.getQuantity() != null ? ci.getQuantity() : 0;
                BigDecimal total = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

                return CartItemDTO.builder()
                                .cartItemId(ci.getCartItemId())
                                .itemId(ci.getItem().getItemId())
                                .itemName(ci.getItem().getName())
                                .quantity(qty)
                                .unitPrice(unit)
                                .totalPrice(total)
                                .build();
        }

        private record PriceAvail(BigDecimal price, boolean available) {
        }

        @Override
        @Transactional(readOnly = true)
        public int getCartCountForUser(String userId, jakarta.servlet.http.HttpSession session) {
                User user = resolveUser(userId);

                // Vì dùng chung giỏ hàng nên không cần branch
                Optional<Cart> opt = cartRepository.findByUser(user);
                if (opt.isEmpty())
                        return 0;

                Cart cart = opt.get();
                return cartItemRepository.findByCart(cart).stream()
                                .mapToInt(ci -> ci.getQuantity() != null ? ci.getQuantity() : 0)
                                .sum();
        }

}
