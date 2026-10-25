package com.movierecommender.repository;

import com.movierecommender.entity.SessionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionResultRepository extends JpaRepository<SessionResult, Long> {
    List<SessionResult> findBySessionIdOrderByRankAsc(Long sessionId);
    List<SessionResult> findBySessionIdAndRankBetweenOrderByRankAsc(Long sessionId, int rankStart, int rankEnd);

    @Query("SELECT sr.title.id FROM SessionResult sr WHERE sr.session.id = :sessionId")
    List<Long> findTitleIdsBySessionId(@Param("sessionId") Long sessionId);
}
