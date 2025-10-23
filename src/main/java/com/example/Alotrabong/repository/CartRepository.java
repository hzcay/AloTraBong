package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.Cart;
import com.example.Alotrabong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    
    Optional<Cart> findByUserAndBranch(User user, Branch branch);
}