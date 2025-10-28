package com.example.Alotrabong.repository;

import com.example.Alotrabong.entity.ShipmentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentEventRepository extends JpaRepository<ShipmentEvent, Long> {
    
    List<ShipmentEvent> findByShipment_ShipmentIdOrderByEventTimeDesc(String shipmentId);
    
    List<ShipmentEvent> findByShipment_ShipmentId(String shipmentId);
}
