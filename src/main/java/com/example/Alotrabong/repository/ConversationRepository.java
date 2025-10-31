package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Override
    @EntityGraph(attributePaths = {"branch", "user"})
    Optional<Conversation> findById(String convoId);

    @EntityGraph(attributePaths = {"branch", "user"})
    Optional<Conversation> findByUser_UserIdAndBranch_BranchIdAndStatus(String userId, String branchId, Byte status);

    @EntityGraph(attributePaths = {"branch", "user"})
    List<Conversation> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = {"branch", "user"})
    List<Conversation> findByBranch_BranchIdAndStatusOrderByCreatedAtDesc(String branchId, Byte status);
}


