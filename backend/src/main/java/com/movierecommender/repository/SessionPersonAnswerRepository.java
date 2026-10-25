package com.movierecommender.repository;

import com.movierecommender.entity.SessionPersonAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionPersonAnswerRepository extends JpaRepository<SessionPersonAnswer, Long> {
    List<SessionPersonAnswer> findBySessionIdOrderByPersonIndex(Long sessionId);
    Optional<SessionPersonAnswer> findBySessionIdAndPersonIndex(Long sessionId, Integer personIndex);
}
