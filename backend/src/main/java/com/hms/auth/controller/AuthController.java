package com.hms.auth.controller;

import com.hms.auth.dto.request.ChangePasswordRequest;
import com.hms.auth.dto.request.LoginRequest;
import com.hms.auth.dto.request.RegisterRequest;
import com.hms.auth.dto.request.TokenRefreshRequest;
import com.hms.auth.dto.response.AuthResponse;
import com.hms.auth.service.AuthService;
import com.hms.security.jwt.CookieUtil;
import com.hms.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class    AuthController {

    private final AuthService service;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ApiResponse<String> register(
            @Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse loginResponse = service.login(request);
        cookieUtil.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            @RequestBody(required = false) TokenRefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response)
    {
        String refreshToken = cookieUtil.getRefreshToken(httpRequest)
                .orElseGet(() -> request != null ? request.getRefreshToken() : null);

        TokenRefreshRequest updatedRequest = TokenRefreshRequest.builder()
                .refreshToken(refreshToken)
                .build();
        AuthResponse refreshResponse = service.refreshToken(updatedRequest);
        
        cookieUtil.setRefreshTokenCookie(response, refreshResponse.getRefreshToken());
        
        return ApiResponse.success(refreshResponse);
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
        return ApiResponse.success("Password changed successfully");
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String refreshToken = cookieUtil.getRefreshToken(request).orElse(null);
        if (authentication != null && authentication.isAuthenticated()) {
            service.logout(authentication.getName(), refreshToken);
        }
        cookieUtil.clearAuthCookies(response);
        return ApiResponse.success("Logged out successfully");
    }

}
