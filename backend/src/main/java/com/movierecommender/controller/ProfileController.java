package com.movierecommender.controller;

import com.movierecommender.dto.profile.ProfileRequest;
import com.movierecommender.dto.profile.ProfileResponse;
import com.movierecommender.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse getProfile(@AuthenticationPrincipal Long userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping
    public ProfileResponse upsertProfile(@AuthenticationPrincipal Long userId, @Valid @RequestBody ProfileRequest request) {
        return profileService.upsertProfile(userId, request);
    }
}
