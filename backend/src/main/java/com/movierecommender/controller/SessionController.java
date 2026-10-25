package com.movierecommender.controller;

import com.movierecommender.dto.session.SessionResultsResponse;
import com.movierecommender.dto.session.SessionStartRequest;
import com.movierecommender.dto.session.SessionStartResponse;
import com.movierecommender.dto.session.SessionSubmitRequest;
import com.movierecommender.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionStartResponse start(@AuthenticationPrincipal Long userId, @Valid @RequestBody SessionStartRequest request) {
        return sessionService.start(userId, request);
    }

    @PostMapping("/{id}/submit")
    public SessionResultsResponse submit(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                          @Valid @RequestBody SessionSubmitRequest request) {
        return sessionService.submit(userId, id, request);
    }

    @PostMapping("/{id}/more")
    public SessionResultsResponse more(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return sessionService.more(userId, id);
    }

    @GetMapping("/{id}/results")
    public SessionResultsResponse getResults(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return sessionService.getResults(userId, id);
    }
}
