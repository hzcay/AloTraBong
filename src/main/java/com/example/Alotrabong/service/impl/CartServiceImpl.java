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

        private static final int MAX_QTY = 99; // chặn spam số lượng

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final ItemRepository itemRepository;

        // Giá theo chi nhánh + inventory
        private final BranchItemPriceRepository branchItemPriceRepository;
        private final InventoryRepository inventoryRepository;

        // ==================== PUBLIC API ====================

        @Override
        public CartItemDTO addToCart(String userIdOrLogin, AddToCartRequest request) {
                log.info("Adding item to cart | user={}, item={}, branch={}, qty={}",
                                userIdOrLogin, request.getItemId(), request.getBranchId(), request.getQuantity());

                int wantQty = (request.getQuantity() == null || request.getQuantity() <= 0) ? 1 : request.getQuantity();
                wantQty = Math.min(wantQty, MAX_QTY);

                User user = resolveUser(userIdOrLogin);
                Branch branch = resolveBranch(request.getBranchId()); // id / code / fallback
                Item item = itemRepository.findById(request.getItemId())
                                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

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

                Cart cart = getOrCreateCart(user, branch);

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
                Branch branch = resolveBranch(branchIdOrCode);

                Optional<Cart> opt = cartRepository.findByUserAndBranch(user, branch);
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

                PriceAvail pa = resolvePriceAndAvailability(cartItem.getItem(), cartItem.getCart().getBranch());
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
                log.info("Removing cart item | user={}, cartItemId={}", userIdOrLogin, cartItemId);

                CartItem cartItem = cartItemRepository.findById(cartItemId)
                                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

                ensureOwnership(userIdOrLogin, cartItem.getCart());
                cartItemRepository.delete(cartItem);
        }

        @Override
        public void clearCart(String userIdOrLogin, String branchIdOrCode) {
                log.info("Clearing cart | user={}, branch={}", userIdOrLogin, branchIdOrCode);

                User user = resolveUser(userIdOrLogin);
                Branch branch = resolveBranch(branchIdOrCode);

                Optional<Cart> opt = cartRepository.findByUserAndBranch(user, branch);
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

        // ==================== PRIVATE HELPERS ====================

        // Cho phép userIdOrLogin là UUID (PK) hoặc email/phone
        private User resolveUser(String userIdOrLogin) {
                if (userIdOrLogin == null || userIdOrLogin.isBlank()) {
                        throw new ResourceNotFoundException("User not found");
                }
                return userRepository.findById(userIdOrLogin)
                                .or(() -> userRepository.findByLogin(userIdOrLogin))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        // Resolve branch theo: ID -> branchCode -> fallback chi nhánh active đầu tiên
        private Branch resolveBranch(String idOrCode) {
                // 1) Theo ID (branch_id)
                if (idOrCode != null && !idOrCode.isBlank()) {
                        Optional<Branch> byId = branchRepository.findById(idOrCode);
                        if (byId.isPresent())
                                return byId.get();

                        // 2) Theo code (branch_code)
                        try {
                                Optional<Branch> byCode = branchRepository.findByBranchCodeIgnoreCase(idOrCode);
                                if (byCode.isPresent())
                                        return byCode.get();
                        } catch (Throwable ignore) {
                                // method có thể chưa được thêm ở repo -> bỏ qua
                        }
                }

                // 3) Fallback: chi nhánh active đầu tiên (ưu tiên repo method nếu có)
                try {
                        return branchRepository.findFirstByIsActiveTrueOrderByCreatedAtAsc()
                                        .orElseGet(this::fallbackActiveBranchFromMemory);
                } catch (Throwable ignore) {
                        return fallbackActiveBranchFromMemory();
                }
        }

        private Branch fallbackActiveBranchFromMemory() {
                return branchRepository.findAll().stream()
                                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        }

        private Cart getOrCreateCart(User user, Branch branch) {
                return cartRepository.findByUserAndBranch(user, branch)
                                .orElseGet(() -> cartRepository.save(
                                                Cart.builder().user(user).branch(branch).build()));
        }

        private void ensureOwnership(String userIdOrLogin, Cart cart) {
                if (cart == null || cart.getUser() == null) {
                        throw new BadRequestException("Unauthorized access to cart");
                }
                User current = resolveUser(userIdOrLogin);
                if (!Objects.equals(cart.getUser().getUserId(), current.getUserId())) {
                        throw new BadRequestException("Unauthorized access to cart");
                }
        }

        private static BigDecimal nz(BigDecimal v) {
                return v != null ? v : BigDecimal.ZERO;
        }

        // Giá theo chi nhánh + available
        private PriceAvail resolvePriceAndAvailability(Item item, Branch branch) {
                BigDecimal price = item.getPrice();
                boolean available = Boolean.TRUE.equals(item.getIsActive());

                var bipOpt = branchItemPriceRepository.findByItemAndBranch(item, branch);
                if (bipOpt.isPresent()) {
                        BranchItemPrice bip = bipOpt.get();
                        if (bip.getPrice() != null)
                                price = bip.getPrice();
                        if (bip.getIsAvailable() != null)
                                available = available && bip.getIsAvailable();
                }
                return new PriceAvail(price, available);
        }

        // Tồn kho đủ không (nếu không có record inventory thì coi như đủ)
        private boolean hasSufficientInventory(String branchId, String itemId, int wantQty) {
                return inventoryRepository.findByBranch_BranchIdAndItem_ItemId(branchId, itemId)
                                .map(inv -> inv.getQuantity() != null && inv.getQuantity() >= wantQty)
                                .orElse(true);
        }

        // Tổng extra từ options trên 1 cartItem
        private BigDecimal calcOptionExtras(CartItem ci) {
                if (ci.getOptions() == null || ci.getOptions().isEmpty())
                        return BigDecimal.ZERO;
                return ci.getOptions().stream()
                                .map(o -> nz(o.getExtraPrice()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private CartItemDTO convertToDTO(CartItem ci) {
                BigDecimal unit = nz(ci.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
                int qty = ci.getQuantity() != null ? ci.getQuantity() : 0;
                BigDecimal total = unit.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

                return CartItemDTO.builder()
                                .cartItemId(ci.getCartItemId())
                                .itemId(ci.getItem().getItemId())
                                .itemName(ci.getItem().getName())
                                .quantity(qty)
                                .unitPrice(unit) // đã gồm option extras
                                .totalPrice(total)
                                .build();
        }

        // tiny record
        private record PriceAvail(BigDecimal price, boolean available) {
        }
}
