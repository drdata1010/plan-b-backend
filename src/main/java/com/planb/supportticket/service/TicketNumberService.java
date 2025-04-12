package com.planb.supportticket.service;

import com.planb.supportticket.entity.SequenceGenerator;
import com.planb.supportticket.repository.SequenceGeneratorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketNumberService {
    private final SequenceGeneratorRepository sequenceGeneratorRepository;
    private static final String TICKET_SEQUENCE = "TICKET_SEQUENCE";
    private static final String TICKET_PREFIX = "TK-";

    @Transactional
    public String generateTicketNumber() {
        // Try to find the sequence with a pessimistic write lock
        SequenceGenerator sequence = sequenceGeneratorRepository.findBySequenceName(TICKET_SEQUENCE)
                .orElse(null);

        if (sequence == null) {
            // Initialize the sequence if it doesn't exist
            sequence = new SequenceGenerator(TICKET_SEQUENCE, 2L); // Start with 2 since we already have TK-1
            sequenceGeneratorRepository.save(sequence);
            return TICKET_PREFIX + "1";
        } else {
            // Increment the sequence
            Long nextValue = sequence.getNextValue();
            sequence.setNextValue(nextValue + 1);
            sequenceGeneratorRepository.save(sequence);
            return TICKET_PREFIX + nextValue;
        }
    }
}
