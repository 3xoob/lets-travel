package com.letstravel.service;

import com.letstravel.config.AppProperties;
import com.letstravel.domain.User;
import com.letstravel.domain.enums.UserRole;
import com.letstravel.dto.auth.RegisterRequest;
import com.letstravel.exception.BusinessException;
import com.letstravel.repository.UserRepository;
import com.letstravel.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;
    @Mock AppProperties appProperties;
    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        var req = new RegisterRequest("test@example.com", "password123", "John", "Doe");
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateAccessToken(any())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh_token");
        when(appProperties.getJwt()).thenReturn(new AppProperties.Jwt());

        var res = authService.register(req);

        assertThat(res.accessToken()).isEqualTo("access_token");
        assertThat(res.user().email()).isEqualTo("test@example.com");
        assertThat(res.user().role()).isEqualTo(UserRole.TRAVELER.name());
    }

    @Test
    void register_duplicateEmail_throws() {
        var req = new RegisterRequest("taken@example.com", "pass", "A", "B");
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void toUserDto_mapsAllFields() {
        var user = User.builder()
            .email("u@test.com")
            .firstName("Alice")
            .lastName("Smith")
            .role(UserRole.TRAVELER)
            .isActive(true)
            .build();

        var dto = AuthService.toUserDto(user);

        assertThat(dto.email()).isEqualTo("u@test.com");
        assertThat(dto.firstName()).isEqualTo("Alice");
        assertThat(dto.role()).isEqualTo(UserRole.TRAVELER.name());
    }
}
