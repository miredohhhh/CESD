package com.hjc.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.ResultCode;
import com.hjc.backend.security.JwtAuthenticationFilter;
import com.hjc.backend.security.PermissionAuthorizationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final PermissionAuthorizationManager permissionAuthorizationManager;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/health",
                                "/api/test/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/permissions").authenticated()
                        .requestMatchers(
                                "/api/frontend/**",
                                "/api/material-applications",
                                "/api/material-applications/*/submit",
                                "/api/material-applications/*/withdraw",
                                "/api/material-applications/*/approve",
                                "/api/material-applications/*/reject",
                                "/api/scores/**",
                                "/api/evaluation-categories/**",
                                "/api/evaluation-items/**",
                                "/api/majors/**",
                                "/api/classes/**",
                                "/api/students/**",
                                "/api/users/**",
                                "/api/roles/**",
                                "/api/permissions/**",
                                "/api/system-configs/**"
                        ).access(permissionAuthorizationManager)
                        .anyRequest().permitAll()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeResponse(response, ResultCode.UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeResponse(response, ResultCode.FORBIDDEN, "Forbidden"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Use AuthService for authentication");
        };
    }

    private void writeResponse(jakarta.servlet.http.HttpServletResponse response, ResultCode resultCode, String message)
            throws java.io.IOException {
        response.setStatus(resultCode.getCode());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(resultCode, message)));
    }
}
