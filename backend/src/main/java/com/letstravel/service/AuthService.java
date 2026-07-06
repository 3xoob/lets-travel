package com.letstravel.service;

import com.letstravel.config.AppProperties;
import com.letstravel.domain.ManagerProfile;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.UserRole;
import com.letstravel.dto.auth.*;
import com.letstravel.dto.user.UserDto;
import com.letstravel.exception.BusinessException;
import com.letstravel.repository.ManagerProfileRepository;
import com.letstravel.repository.UserRepository;
import com.letstravel.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ManagerProfileRepository managerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AppProperties appProperties;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT);
        }
        User user = User.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(UserRole.TRAVELER)
            .isActive(true)
            .emailVerified(false)
            .build();
        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException("Invalid credentials", HttpStatus.UNAUTHORIZED));
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        // Find user by iterating stored tokens — we embed email in a lookup via the token
        // In practice: decode email from a non-expiring claim or store email separately
        // Here we require the client to send email too, or use a different approach.
        // Simplified: store the refresh token value → email in Redis
        throw new BusinessException("Refresh token flow: send POST /api/auth/refresh with {refreshToken}", HttpStatus.BAD_REQUEST);
    }

    public AuthResponse refreshByEmail(String email, String refreshToken) {
        if (!jwtUtil.validateRefreshToken(email, refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        jwtUtil.invalidateRefreshToken(email);
        return buildAuthResponse(user);
    }

    public void logout(String email) {
        jwtUtil.invalidateRefreshToken(email);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        UserDto userDto = toUserDto(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer",
            appProperties.getJwt().getAccessTokenExpiryMs() / 1000, userDto);
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFirstName(),
            user.getLastName(), user.getRole().name(), user.getAvatarUrl());
    }
}
