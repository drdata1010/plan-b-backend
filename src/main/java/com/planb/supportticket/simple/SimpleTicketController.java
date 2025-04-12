package com.planb.supportticket.simple;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple ticket controller for testing.
 */
@RestController
@RequestMapping("/api/tickets")
public class SimpleTicketController {

    private final List<Map<String, Object>> tickets = new ArrayList<>();

    /**
     * Get all tickets.
     * 
     * @return a list of tickets
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTickets() {
        return ResponseEntity.ok(tickets);
    }

    /**
     * Create a new ticket.
     * 
     * @param ticket the ticket data
     * @return the created ticket
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTicket(@RequestBody Map<String, Object> ticket) {
        Map<String, Object> newTicket = new HashMap<>(ticket);
        newTicket.put("id", UUID.randomUUID().toString());
        newTicket.put("status", "OPEN");
        newTicket.put("createdAt", System.currentTimeMillis());
        tickets.add(newTicket);
        return ResponseEntity.ok(newTicket);
    }

    /**
     * Get a ticket by ID.
     * 
     * @param id the ticket ID
     * @return the ticket
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTicketById(@PathVariable String id) {
        for (Map<String, Object> ticket : tickets) {
            if (ticket.get("id").equals(id)) {
                return ResponseEntity.ok(ticket);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Update a ticket.
     * 
     * @param id the ticket ID
     * @param ticket the updated ticket data
     * @return the updated ticket
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTicket(@PathVariable String id, @RequestBody Map<String, Object> ticket) {
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).get("id").equals(id)) {
                Map<String, Object> updatedTicket = new HashMap<>(ticket);
                updatedTicket.put("id", id);
                updatedTicket.put("updatedAt", System.currentTimeMillis());
                tickets.set(i, updatedTicket);
                return ResponseEntity.ok(updatedTicket);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete a ticket.
     * 
     * @param id the ticket ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String id) {
        for (int i = 0; i < tickets.size(); i++) {
            if (tickets.get(i).get("id").equals(id)) {
                tickets.remove(i);
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
