package com.hjc.backend.security;

import com.hjc.backend.config.JwtProperties;
import com.hjc.backend.vo.LoginUserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "userId";

    private static final String CLAIM_ROLE_ID = "roleId";

    private static final String CLAIM_ROLE_CODE = "roleCode";

    private static final String CLAIM_ROLE_NAME = "roleName";

    private static final String CLAIM_STUDENT_ID = "studentId";

    private final JwtProperties jwtProperties;

    public String generateToken(LoginUserVO user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(getExpirationSeconds());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getUserId())
                .claim(CLAIM_ROLE_ID, user.getRoleId())
                .claim(CLAIM_ROLE_CODE, user.getRoleCode())
                .claim(CLAIM_ROLE_NAME, user.getRoleName())
                .claim(CLAIM_STUDENT_ID, user.getStudentId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSecretKey())
                .compact();
    }

    public LoginUserContext parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = getLongClaim(claims, CLAIM_USER_ID);
        Long roleId = getLongClaim(claims, CLAIM_ROLE_ID);
        String roleCode = claims.get(CLAIM_ROLE_CODE, String.class);
        String roleName = claims.get(CLAIM_ROLE_NAME, String.class);
        Long studentId = getLongClaim(claims, CLAIM_STUDENT_ID);
        return new LoginUserContext(userId, claims.getSubject(), roleId, roleCode, roleName, studentId);
    }

    public Long getExpirationSeconds() {
        return jwtProperties.getExpirationSeconds() == null ? 86400L : jwtProperties.getExpirationSeconds();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Long getLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            return Long.valueOf(text);
        }
        return null;
    }
}
