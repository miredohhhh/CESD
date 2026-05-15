package com.hjc.backend.security;

public final class LoginUserContextHolder {

    private static final ThreadLocal<LoginUserContext> HOLDER = new ThreadLocal<>();

    private LoginUserContextHolder() {
    }

    public static void set(LoginUserContext context) {
        HOLDER.set(context);
    }

    public static LoginUserContext get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
