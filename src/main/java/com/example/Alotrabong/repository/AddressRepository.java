package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByUser_UserId(String userId);
    List<Address> findByUser_UserIdAndIsDefaultTrue(String userId);
}
