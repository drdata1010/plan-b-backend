package com.planb.supportticket.repository;

import com.planb.supportticket.entity.SequenceGenerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SequenceGenerator> findBySequenceName(String sequenceName);

    /**
     * Checks if a sequence with the given name exists.
     *
     * @param sequenceName the sequence name
     * @return true if the sequence exists, false otherwise
     */
    boolean existsById(String sequenceName);
}
