package ru.senla.scooterrental.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private CustomUserDetailsService customUserDetailsService;

    private JwtAuthenticationFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        customUserDetailsService =
                mock(CustomUserDetailsService.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                customUserDetailsService
        );

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSkip_whenAuthorizationHeaderMissing()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verify(jwtService, never())
                .extractEmail(org.mockito.ArgumentMatchers.any());

        verify(customUserDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.any()
                );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_shouldSkip_whenAuthorizationHeaderIsInvalid()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("InvalidHeader");

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verify(jwtService, never())
                .extractEmail(org.mockito.ArgumentMatchers.any());

        verify(customUserDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.any()
                );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_shouldSkip_whenTokenInvalid()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalidToken");

        when(jwtService.extractEmail("invalidToken"))
                .thenReturn("ivan@example.com");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("ivan@example.com")
                .password("password")
                .authorities(List.of())
                .build();

        when(customUserDetailsService
                .loadUserByUsername("ivan@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("invalidToken"))
                .thenReturn(false);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilterInternal_shouldAuthenticateSuccessfully()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer validToken");

        when(jwtService.extractEmail("validToken"))
                .thenReturn("ivan@example.com");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("ivan@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        when(customUserDetailsService
                .loadUserByUsername("ivan@example.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("validToken"))
                .thenReturn(true);

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        assertEquals(
                "ivan@example.com",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
        );

        assertEquals(
                "ROLE_USER",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );
    }

    @Test
    void doFilterInternal_shouldNotOverrideExistingAuthentication()
            throws ServletException, IOException {

        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(
                        "alreadyLogged",
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(existingAuth);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer validToken");

        when(jwtService.extractEmail("validToken"))
                .thenReturn("ivan@example.com");

        filter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(customUserDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.any()
                );

        verify(filterChain)
                .doFilter(request, response);

        assertEquals(
                "alreadyLogged",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
        );
    }
}