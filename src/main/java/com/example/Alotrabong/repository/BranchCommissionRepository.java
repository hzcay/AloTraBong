package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.BranchCommission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchCommissionRepository extends JpaRepository<BranchCommission, Integer> {

    // Tìm commission đang active cho một chi nhánh
    @Query("SELECT bc FROM BranchCommission bc WHERE bc.branch.branchId = :branchId AND bc.isActive = true ORDER BY bc.effectiveFrom DESC")
    Optional<BranchCommission> findActiveCommissionByBranchId(@Param("branchId") String branchId);

    // Tìm tất cả commission của một chi nhánh (có phân trang)
    Page<BranchCommission> findByBranch_BranchIdOrderByEffectiveFromDesc(String branchId, Pageable pageable);

    // Tìm tất cả commission đang active
    List<BranchCommission> findByIsActiveTrueOrderByBranch_NameAsc();

    // Tìm commission theo branch và thời gian hiệu lực
    @Query("SELECT bc FROM BranchCommission bc WHERE bc.branch.branchId = :branchId " +
           "AND bc.effectiveFrom <= :date " +
           "AND (bc.effectiveTo IS NULL OR bc.effectiveTo >= :date) " +
           "AND bc.isActive = true " +
           "ORDER BY bc.effectiveFrom DESC")
    Optional<BranchCommission> findActiveCommissionByBranchAndDate(@Param("branchId") String branchId, @Param("date") LocalDate date);

    // Đếm số commission đang active
    long countByIsActiveTrue();

    // Đếm số commission của một chi nhánh
    long countByBranch_BranchId(String branchId);

    // Tìm commission sắp hết hạn (trong 30 ngày tới)
    @Query("SELECT bc FROM BranchCommission bc WHERE bc.isActive = true " +
           "AND bc.effectiveTo IS NOT NULL " +
           "AND bc.effectiveTo BETWEEN :today AND :next30Days " +
           "ORDER BY bc.effectiveTo ASC")
    List<BranchCommission> findCommissionsExpiringSoon(@Param("today") LocalDate today, @Param("next30Days") LocalDate next30Days);

    // Tìm commission đã hết hạn
    @Query("SELECT bc FROM BranchCommission bc WHERE bc.isActive = true " +
           "AND bc.effectiveTo IS NOT NULL " +
           "AND bc.effectiveTo < :today")
    List<BranchCommission> findExpiredCommissions(@Param("today") LocalDate today);

    // Tìm commission theo loại
    List<BranchCommission> findByCommissionTypeAndIsActiveTrueOrderByBranch_NameAsc(BranchCommission.CommissionType commissionType);

    // Tìm commission theo giá trị (range)
    @Query("SELECT bc FROM BranchCommission bc WHERE bc.isActive = true " +
           "AND bc.commissionValue BETWEEN :minValue AND :maxValue " +
           "ORDER BY bc.commissionValue DESC")
    List<BranchCommission> findByCommissionValueRange(@Param("minValue") java.math.BigDecimal minValue, 
                                                      @Param("maxValue") java.math.BigDecimal maxValue);
}
