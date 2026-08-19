package com.raileasy.user;

import com.raileasy.common.ConflictException;
import com.raileasy.common.UnauthorizedException;
import com.raileasy.security.JwtService;
import com.raileasy.user.dto.AuthResponse;
import com.raileasy.user.dto.LoginRequest;
import com.raileasy.user.dto.RegisterRequest;
import com.raileasy.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("rider@raileasy.com", "hashed", "Rider", false);
    }

    @Test
    void register_savesUser_withEncodedPassword_whenEmailNotTaken() {
        RegisterRequest request = new RegisterRequest("rider@raileasy.com", "Passw0rd", "Rider");
        when(userRepository.existsByEmail("rider@raileasy.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("rider@raileasy.com");
        assertThat(response.name()).isEqualTo("Rider");
        assertThat(response.isAdmin()).isFalse();
        verify(passwordEncoder).encode("Passw0rd");
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("rider@raileasy.com", "Passw0rd", "Rider");
        when(userRepository.existsByEmail("rider@raileasy.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenAndUser_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("rider@raileasy.com", "Passw0rd");
        when(userRepository.findByEmail("rider@raileasy.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Passw0rd", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("rider@raileasy.com");
    }

    @Test
    void login_throwsUnauthorized_whenUserNotFound() {
        LoginRequest request = new LoginRequest("nobody@raileasy.com", "Passw0rd");
        when(userRepository.findByEmail("nobody@raileasy.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_throwsUnauthorized_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("rider@raileasy.com", "wrong-password");
        when(userRepository.findByEmail("rider@raileasy.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
        verify(jwtService, never()).generateToken(any());
    }
}
