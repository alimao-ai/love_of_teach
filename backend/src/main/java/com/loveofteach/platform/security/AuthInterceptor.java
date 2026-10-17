package com.loveofteach.platform.security;

import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.domain.AuthToken;
import com.loveofteach.platform.repository.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 校验 Bearer Token 并把当前用户写入请求上下文。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenRepository authTokenRepository;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("请先登录", HttpStatus.UNAUTHORIZED);
        }
        String token = authorization.substring(7).trim();
        AuthToken authToken = authTokenRepository.findActiveToken(token, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("登录已失效，请重新登录", HttpStatus.UNAUTHORIZED));
        request.setAttribute(AuthContext.REQUEST_ATTRIBUTE, RequestUser.from(authToken.getUser()));
        return true;
    }
}
