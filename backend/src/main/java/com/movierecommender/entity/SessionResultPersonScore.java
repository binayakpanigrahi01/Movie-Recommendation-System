package com.movierecommender.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "session_result_person_scores",
    uniqueConstraints = @UniqueConstraint(name = "uk_result_person", columnNames = {"session_result_id", "person_index"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResultPersonScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_result_id", nullable = false)
    private SessionResult sessionResult;

    @Column(name = "person_index", nullable = false)
    private Integer personIndex;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;
}
