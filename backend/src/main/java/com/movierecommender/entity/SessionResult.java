package com.movierecommender.entity;

import com.movierecommender.entity.enums.ResultBadge;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "session_results",
    uniqueConstraints = @UniqueConstraint(name = "uk_session_title", columnNames = {"session_id", "title_id"}),
    indexes = @Index(name = "idx_session_results_session_rank", columnList = "session_id, rank")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(name = "`rank`", nullable = false)
    private Integer rank;

    @Column(name = "group_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal groupScore;

    @Builder.Default
    @Column(nullable = false)
    private ResultBadge badge = ResultBadge.NONE;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "sessionResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionResultPersonScore> personScores = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
