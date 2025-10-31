package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @EntityGraph(attributePaths = {"sender"})
    Page<Message> findByConversation_ConvoIdOrderBySentAtDesc(String convoId, Pageable pageable);
}


