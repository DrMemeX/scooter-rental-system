package ru.senla.scooterrental.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.senla.scooterrental.user.entity.User;
import ru.senla.scooterrental.user.enums.Role;
import ru.senla.scooterrental.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = user(
                "ivan@example.com",
                "encodedPassword",
                Role.USER,
                false
        );

        when(userRepository.findByEmail("ivan@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("ivan@example.com");

        assertEquals("ivan@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));
        assertTrue(result.isAccountNonLocked());

        verify(userRepository).findByEmail("ivan@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnAdminAuthority_whenUserIsAdmin() {
        User user = user(
                "admin@example.com",
                "encodedPassword",
                Role.ADMIN,
                false
        );

        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin@example.com");

        assertTrue(result.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(result.isAccountNonLocked());

        verify(userRepository).findByEmail("admin@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnLockedAccount_whenUserIsBlocked() {
        User user = user(
                "blocked@example.com",
                "encodedPassword",
                Role.USER,
                true
        );

        when(userRepository.findByEmail("blocked@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("blocked@example.com");

        assertEquals("blocked@example.com", result.getUsername());
        assertFalse(result.isAccountNonLocked());
        assertTrue(result.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByEmail("blocked@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("unknown@example.com")
        );

        assertEquals(
                "Пользователь с email unknown@example.com не найден",
                exception.getMessage()
        );

        verify(userRepository).findByEmail("unknown@example.com");
    }

    @Test
    void loadUserByUsername_shouldCallRepositoryWithProvidedEmail() {
        User user = user(
                "test@example.com",
                "password",
                Role.USER,
                false
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        service.loadUserByUsername("test@example.com");

        verify(userRepository).findByEmail("test@example.com");
    }

    private User user(String email,
                      String password,
                      Role role,
                      boolean blocked) {
        User user = mock(User.class);

        lenient().when(user.getEmail()).thenReturn(email);
        lenient().when(user.getPassword()).thenReturn(password);
        lenient().when(user.getRole()).thenReturn(role);
        lenient().when(user.isBlocked()).thenReturn(blocked);

        return user;
    }
}