package com.movierecommender.service;

import com.movierecommender.dto.auth.AuthResponse;
import com.movierecommender.dto.auth.ForgotPasswordRequest;
import com.movierecommender.dto.auth.LoginRequest;
import com.movierecommender.dto.auth.ResetPasswordRequest;
import com.movierecommender.dto.auth.SignupRequest;
import com.movierecommender.dto.auth.UserDto;
import com.movierecommender.entity.PasswordResetToken;
import com.movierecommender.entity.User;
import com.movierecommender.exception.ApiException;
import com.movierecommender.repository.PasswordResetTokenRepository;
import com.movierecommender.repository.UserRepository;
import com.movierecommender.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED",
                    "An account with this email already exists.");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);
        return toAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Invalid email or password."));
        return toAuthResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteAllByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build();
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "RESET_TOKEN_INVALID",
                        "This reset link is invalid."));
        if (resetToken.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESET_TOKEN_INVALID",
                    "This reset link has already been used.");
        }
        if (resetToken.isExpired()) {
            throw new ApiException(HttpStatus.GONE, "RESET_TOKEN_EXPIRED", "This reset link has expired.");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtTokenProvider.generateToken(user.getId());
        return new AuthResponse(token, new UserDto(user.getId(), user.getName(), user.getEmail()));
    }
}
