package com.hjc.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjc.backend.common.ApiResponse;
import com.hjc.backend.common.ResultCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        try {
            if (StringUtils.hasText(token)) {
                LoginUserContext context = jwtTokenProvider.parseToken(token);
                LoginUserContextHolder.set(context);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        context,
                        null,
                        buildAuthorities(context)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            LoginUserContextHolder.clear();
            writeUnauthorizedResponse(response, "登录已过期或 token 无效");
        } finally {
            SecurityContextHolder.clearContext();
            LoginUserContextHolder.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

    private List<SimpleGrantedAuthority> buildAuthorities(LoginUserContext context) {
        if (!StringUtils.hasText(context.getRoleCode())) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority(context.getRoleCode().toUpperCase()));
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(ResultCode.UNAUTHORIZED, message)));
    }
}
