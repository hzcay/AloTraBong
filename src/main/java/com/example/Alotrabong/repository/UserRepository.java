package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("""
            select u from User u
            where lower(u.email) = lower(:login) or u.phone = :login
            """)
    Optional<User> findByLogin(@Param("login") String login);

    @Query("""
            select case when count(u) > 0 then true else false end
            from User u
            where lower(u.email) = lower(:login) or u.phone = :login
            """)
    boolean existsByLogin(@Param("login") String login);

    @Query("""
        SELECT u FROM User u
        JOIN u.userRoles ur
        JOIN ur.role r
        WHERE r.roleCode = 'BRANCH_MANAGER'
        """)
    List<User> findAllManagers();

    long countByIsActive(Boolean isActive);
}
