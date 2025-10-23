package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Cart;
import com.example.Alotrabong.entity.CartItem;
import com.example.Alotrabong.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    List<CartItem> findByCart(Cart cart);
    
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);
    
    void deleteByCart(Cart cart);
}
