package com.loveofteach.platform.security;

import com.loveofteach.platform.common.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

public final class AuthContext {

    public static final String REQUEST_ATTRIBUTE = "currentUser";

    private AuthContext() {
    }

    public static RequestUser require(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        if (value instanceof RequestUser user) {
            return user;
        }
        throw new BusinessException("请先登录", HttpStatus.UNAUTHORIZED);
    }
}
