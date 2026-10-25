package com.movierecommender.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String resetPasswordUrl;

    public EmailService(JavaMailSender mailSender,
                         @Value("${spring.mail.username:}") String fromAddress,
                         @Value("${app.frontend.reset-password-url}") String resetPasswordUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.resetPasswordUrl = resetPasswordUrl;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = resetPasswordUrl + "?token=" + token;
        if (fromAddress == null || fromAddress.isBlank()) {
            log.info("SMTP not configured — password reset link for {}: {}", toEmail, link);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your Movie Recommender password");
        message.setText("Click the link below to reset your password. It expires in 30 minutes.\n\n" + link);
        mailSender.send(message);
    }
}
