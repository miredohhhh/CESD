package com.hjc.backend.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

public final class LogContextUtils {

    private static final int MAX_TEXT_LENGTH = 2000;

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(password|oldPassword|newPassword|confirmPassword|token|authorization)\\s*[=:]\\s*[^,}&\\s]+");

    private LogContextUtils() {
    }

    public static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    public static String requestMethod() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getMethod();
    }

    public static String requestUri() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getRequestURI();
    }

    public static String ipAddress() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return limit(forwarded.split(",")[0].trim(), 100);
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return limit(realIp, 100);
        }
        return limit(request.getRemoteAddr(), 100);
    }

    public static String userAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : limit(request.getHeader("User-Agent"), 500);
    }

    public static String sanitize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return limit(SENSITIVE_PATTERN.matcher(value).replaceAll("$1=***"), MAX_TEXT_LENGTH);
    }

    public static String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
