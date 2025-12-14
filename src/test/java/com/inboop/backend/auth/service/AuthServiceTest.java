package com.inboop.backend.auth.service;

import com.inboop.backend.auth.dto.AuthResponse;
import com.inboop.backend.auth.dto.LoginRequest;
import com.inboop.backend.auth.dto.RegisterRequest;
import com.inboop.backend.auth.entity.User;
import com.inboop.backend.auth.repository.UserRepository;
import com.inboop.backend.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Set the jwtExpiration field using reflection
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("USER");
        testUser.setOauthProvider("LOCAL");

        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    // ==================== Register Tests ====================

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("Test User", response.getUser().getName());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.register(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_SetsCorrectUserFields() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            // Verify the user fields are set correctly
            assertEquals("Test User", savedUser.getName());
            assertEquals("test@example.com", savedUser.getEmail());
            assertEquals("encodedPassword", savedUser.getPassword());
            assertEquals("USER", savedUser.getRole());
            assertEquals("LOCAL", savedUser.getOauthProvider());
            return savedUser;
        });
        when(jwtUtil.generateToken(anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        authService.register(registerRequest);

        verify(userRepository).save(any(User.class));
    }

    // ==================== Login Tests ====================

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("Test User", response.getUser().getName());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () ->
            authService.login(loginRequest)
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ==================== Refresh Token Tests ====================

    @Test
    void refreshToken_Success() {
        String refreshToken = "validRefreshToken";

        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(refreshToken, "test@example.com")).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("newRefreshToken");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());

        verify(jwtUtil).isRefreshToken(refreshToken);
        verify(jwtUtil).extractUsername(refreshToken);
        verify(jwtUtil).validateToken(refreshToken, "test@example.com");
    }

    @Test
    void refreshToken_InvalidRefreshToken_ThrowsException() {
        String invalidToken = "notARefreshToken";

        when(jwtUtil.isRefreshToken(invalidToken)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.refreshToken(invalidToken)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(jwtUtil).isRefreshToken(invalidToken);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void refreshToken_UserNotFound_ThrowsException() {
        String refreshToken = "validRefreshToken";

        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.refreshToken(refreshToken)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsException() {
        String refreshToken = "expiredRefreshToken";

        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(refreshToken, "test@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.refreshToken(refreshToken)
        );

        assertEquals("Invalid or expired refresh token", exception.getMessage());
    }

    // ==================== Find By Email Tests ====================

    @Test
    void findByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = authService.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        assertEquals("Test User", foundUser.getName());
    }

    @Test
    void findByEmail_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.findByEmail("unknown@example.com")
        );

        assertEquals("User not found", exception.getMessage());
    }

    // ==================== Auth Response Tests ====================

    @Test
    void authResponse_ContainsCorrectExpiresIn() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        // jwtExpiration is 86400000ms, divided by 1000 = 86400 seconds
        assertEquals(86400, response.getExpiresIn());
    }

    @Test
    void authResponse_ContainsUserRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refreshToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response.getUser());
        assertEquals("USER", response.getUser().getRole());
    }
}
