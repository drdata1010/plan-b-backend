package com.example.supportticketsystemapi.repository;

import com.example.supportticketsystemapi.entity.Ticket;
import com.example.supportticketsystemapi.entity.TicketStatus;
import com.example.supportticketsystemapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy(User user);
    
    List<Ticket> findByAssignedTo(User user);
    
    List<Ticket> findByStatus(TicketStatus status);
}
