package com.movierecommender.repository;

import com.movierecommender.entity.SessionResultPersonScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionResultPersonScoreRepository extends JpaRepository<SessionResultPersonScore, Long> {
    List<SessionResultPersonScore> findBySessionResultId(Long sessionResultId);
}
