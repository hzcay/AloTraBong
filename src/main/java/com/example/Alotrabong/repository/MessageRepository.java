package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversation_ConvoIdOrderByCreatedAtAsc(String convoId);
}
