package com.hms.auth.service.impl;

import com.hms.common.enums.Department;
import com.hms.common.enums.Role;
import com.hms.doctor.repository.DoctorRepository;
import com.hms.doctor.entity.Doctor;
import com.hms.auth.dto.request.ChangePasswordRequest;
import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.request.TokenRefreshRequest;
import com.hms.auth.dto.response.AuthResponse;
import com.hms.auth.entity.RevokedRefreshToken;
import com.hms.auth.repository.RevokedRefreshTokenRepository;
import com.hms.auth.service.AuthService;
import com.hms.common.audit.AuditLogService;
import com.hms.user.exception.EmailAlreadyExistsException;
import com.hms.user.exception.InvalidCredentialsException;
import com.hms.user.exception.UserNotFoundException;
import com.hms.user.exception.UsernameAlreadyExistsException;
import com.hms.security.jwt.JwtUtil;
import com.hms.user.entity.User;
import com.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final DoctorRepository doctorRepository;
    private final RevokedRefreshTokenRepository revokedRefreshTokenRepository;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setTokenVersion(0);
        user.setEnabled(true);
        user.setPasswordChangeRequired(false);

        User savedUser = userRepository.save(user);

        // Auto-create Doctor profile if role is DOCTOR
        if (request.getRole() == Role.DOCTOR) {
            doctorRepository.save(Doctor.builder()
                    .userId(savedUser.getId())
                    .firstName(savedUser.getUsername())
                    .consultationFee(new BigDecimal("50.00"))
                    .lastName("General Doctor")
                    .specialization("General Medicine")
                    .department(Department.GENERAL_MEDICINE)
                    .registrationNumber("REG-" + savedUser.getId())
                    .email(savedUser.getEmail())
                    .isAvailable(true)
                    .build());
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        auditLogService.log(
                user.getUsername(),
                "USER_LOGIN",
                "User",
                user.getId().toString(),
                "role=" + user.getRole()
        );

        return AuthResponse.builder()
                .token(jwtUtil.generateAccessToken( user.getUsername(),
                                                    user.getRole().name(),
                                                    user.getTokenVersion()
                ))
                .refreshToken(jwtUtil.generateRefreshToken( user.getUsername(),
                                                            user.getTokenVersion()
                ))
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .build();
    }

    @Override
    public AuthResponse refreshToken(TokenRefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        if (revokedRefreshTokenRepository.existsByTokenHash(jwtUtil.hashToken(refreshToken))) {
            throw new InvalidCredentialsException("Refresh token has been revoked. Please log in again.");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Integer tokenVersion = jwtUtil.extractTokenVersion(refreshToken);
        if (!user.getTokenVersion().equals(tokenVersion)) {
            throw new InvalidCredentialsException("Token version mismatch. Please log in again.");
        }

        return AuthResponse.builder()
                .token(jwtUtil.generateAccessToken(user.getUsername(), user.getRole().name(), user.getTokenVersion()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getUsername(), user.getTokenVersion()))
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .passwordChangeRequired(user.getPasswordChangeRequired())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangeRequired(false);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        auditLogService.log(username, "PASSWORD_CHANGE", "User", user.getId().toString(), null);
    }

    @Override
    public void logout(String username, String refreshToken) {

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            auditLogService.log(username, "USER_LOGOUT", "User", null, "userMissing=true");
            return;
        }

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        revokeRefreshTokenIfPresent(refreshToken, username);
        auditLogService.log(username, "USER_LOGOUT", "User", user.getId().toString(), null);
    }

    private void revokeRefreshTokenIfPresent(String refreshToken, String username) {
        if (refreshToken == null ||
                refreshToken.isBlank() ||
                !jwtUtil.validateToken(refreshToken)) {
            return;
        }

        String tokenHash = jwtUtil.hashToken(refreshToken);
        if (revokedRefreshTokenRepository.existsByTokenHash(tokenHash)) {
            return;
        }

        // Saving revoked refresh token in db => should never be used again
        revokedRefreshTokenRepository.save(RevokedRefreshToken.builder()
                .tokenHash(tokenHash)
                .username(username)
                .expiresAt(LocalDateTime.ofInstant(
                        jwtUtil.extractExpiration(refreshToken)
                                .toInstant(), ZoneId.systemDefault()))
                .build());
    }
}
