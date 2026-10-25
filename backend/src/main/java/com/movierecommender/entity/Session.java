package com.movierecommender.entity;

import com.movierecommender.entity.enums.Occasion;
import com.movierecommender.entity.enums.SessionMediaType;
import com.movierecommender.entity.enums.SessionStatus;
import com.movierecommender.entity.enums.TimeLimit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "sessions",
    indexes = @Index(name = "idx_sessions_user_id", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "media_type", nullable = false)
    private SessionMediaType mediaType;

    @Column(name = "group_size", nullable = false)
    private Integer groupSize;

    @Column(name = "time_limit", nullable = false)
    private TimeLimit timeLimit;

    @Column(name = "occasion")
    private Occasion occasion;

    @Builder.Default
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionPersonAnswer> personAnswers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionResult> results = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
