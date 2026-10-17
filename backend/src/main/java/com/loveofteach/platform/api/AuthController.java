package com.loveofteach.platform.api;

import com.loveofteach.platform.api.dto.AuthResponse;
import com.loveofteach.platform.api.dto.LoginRequest;
import com.loveofteach.platform.api.dto.RegisterRequest;
import com.loveofteach.platform.api.dto.UserResponse;
import com.loveofteach.platform.security.AuthContext;
import com.loveofteach.platform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        authService.logout(authorization == null ? null : authorization.replaceFirst("^Bearer\\s+", ""));
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        return authService.me(AuthContext.require(request));
    }
}
