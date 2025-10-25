package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.entity.UserRole;
import com.example.Alotrabong.entity.Role;
import com.example.Alotrabong.entity.Branch;
import com.example.Alotrabong.entity.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUser(User user);
    List<UserRole> findByRole(Role role);
    void deleteByUser(User user);
    void deleteByUserAndRole(User user, Role role);
    boolean existsByUserAndRole(User user, Role role);

    // Branch manager specific queries
    Optional<UserRole> findByBranchAndRole_RoleCode(Branch branch, RoleCode roleCode);

    List<UserRole> findByBranch(Branch branch);

    @Query("SELECT ur FROM UserRole ur WHERE ur.branch.branchId = :branchId AND ur.role.roleCode = :roleCode")
    Optional<UserRole> findManagerByBranchId(@Param("branchId") String branchId, @Param("roleCode") RoleCode roleCode);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.branch.branchId = :branchId AND ur.role.roleCode = :roleCode")
    void deleteManagerByBranchId(@Param("branchId") String branchId, @Param("roleCode") RoleCode roleCode);

    boolean existsByUserAndBranch(User user, Branch branch);
}
