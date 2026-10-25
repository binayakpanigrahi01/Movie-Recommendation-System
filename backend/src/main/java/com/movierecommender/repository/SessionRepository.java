package com.movierecommender.repository;

import com.movierecommender.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Session> findByIdAndUserId(Long id, Long userId);
}
