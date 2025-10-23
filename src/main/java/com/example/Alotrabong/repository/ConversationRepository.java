package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByUser_UserIdAndStatus(String userId, Byte status);

    List<Conversation> findByBranch_BranchIdAndStatus(String branchId, Byte status);
}
