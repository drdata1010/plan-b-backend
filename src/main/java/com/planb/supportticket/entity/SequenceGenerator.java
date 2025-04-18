package com.planb.supportticket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sequence_generators")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SequenceGenerator {
    @Id
    @Column(name = "sequence_name", nullable = false)
    private String sequenceName;
    
    @Column(name = "next_value", nullable = false)
    private Long nextValue;
}
