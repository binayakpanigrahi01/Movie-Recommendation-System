package com.movierecommender.service;

import com.movierecommender.dto.profile.ProfileRequest;
import com.movierecommender.dto.profile.ProfileResponse;
import com.movierecommender.entity.User;
import com.movierecommender.entity.UserProfile;
import com.movierecommender.exception.ApiException;
import com.movierecommender.repository.UserProfileRepository;
import com.movierecommender.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = getUser(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND",
                        "Profile not set up yet."));
        return toResponse(user, profile);
    }

    @Transactional
    public ProfileResponse upsertProfile(Long userId, ProfileRequest request) {
        User user = getUser(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());
        profile.setBirthYear(request.birthYear());
        profile.setSubtitlesOk(request.subtitlesOk());
        profile.setLanguages(request.languages());
        profile.setFavoriteGenres(request.favoriteGenres());
        profile.setNeverWantGenres(request.neverWantGenres());
        profile.setLovedTitleIds(request.lovedTitleIds() == null ? new ArrayList<>() : request.lovedTitleIds());
        profile.setEraPreference(request.eraPreference());
        profile = userProfileRepository.save(profile);
        return toResponse(user, profile);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_INVALID", "User not found."));
    }

    private ProfileResponse toResponse(User user, UserProfile profile) {
        return new ProfileResponse(
                user.getName(),
                user.getEmail(),
                profile.getBirthYear(),
                profile.isSubtitlesOk(),
                profile.getLanguages(),
                profile.getFavoriteGenres(),
                profile.getNeverWantGenres(),
                profile.getLovedTitleIds(),
                profile.getEraPreference());
    }
}
