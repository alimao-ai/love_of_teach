package com.loveofteach.platform.service;

import com.loveofteach.platform.api.dto.AuthResponse;
import com.loveofteach.platform.api.dto.LoginRequest;
import com.loveofteach.platform.api.dto.RegisterRequest;
import com.loveofteach.platform.api.dto.UserResponse;
import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.AuthToken;
import com.loveofteach.platform.domain.Teacher;
import com.loveofteach.platform.domain.UserAccount;
import com.loveofteach.platform.repository.AuthTokenRepository;
import com.loveofteach.platform.repository.TeacherRepository;
import com.loveofteach.platform.repository.UserAccountRepository;
import com.loveofteach.platform.security.RequestUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/**
 * 处理账号注册、登录、令牌签发和当前身份查询。
 * 密码只保存 BCrypt 哈希，教师账号必须绑定已有教师档案。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final AuthTokenRepository authTokenRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.auth.token-ttl-hours:24}")
    private long tokenTtlHours;

    @Value("${app.auth.teacher-registration-code:}")
    private String teacherRegistrationCode;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (request.role() != UserRole.TEACHER) {
            throw new BusinessException("公开注册仅允许教师角色，其他角色由管理员创建");
        }
        verifyRegistrationCode(request.registrationCode());
        Teacher teacher = null;
        if (request.role() == UserRole.TEACHER) {
            if (!StringUtils.hasText(request.teacherNo())) {
                throw new BusinessException("教师注册必须填写教师工号");
            }
            teacher = teacherRepository.findByEmployeeNoIgnoreCase(request.teacherNo().trim())
                    .orElseThrow(() -> new BusinessException("教师工号不存在"));
            if (userAccountRepository.existsByTeacherId(teacher.getId())) {
                throw new BusinessException("该教师工号已绑定账号");
            }
        }
        UserAccount account;
        try {
            account = userAccountRepository.saveAndFlush(UserAccount.builder()
                    .username(username)
                    .displayName(request.displayName().trim())
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .role(request.role())
                    .teacher(teacher)
                    .active(true)
                    .build());
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    "用户名或教师工号已被绑定", HttpStatus.CONFLICT);
        }
        return issueToken(account);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String sourceAddress) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String attemptKey = username + "|" + sourceAddress;
        loginAttemptService.assertAllowed(attemptKey);
        try {
            UserAccount account = userAccountRepository
                    .findByUsernameIgnoreCase(username)
                    .filter(UserAccount::isActive)
                    .orElseThrow(() -> new BusinessException(
                            "用户名或密码错误", HttpStatus.UNAUTHORIZED));
            if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
                throw new BusinessException("用户名或密码错误", HttpStatus.UNAUTHORIZED);
            }
            loginAttemptService.recordSuccess(attemptKey);
            authTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
            return issueToken(account);
        } catch (BusinessException exception) {
            loginAttemptService.recordFailure(attemptKey);
            throw exception;
        }
    }

    @Transactional
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            authTokenRepository.deleteByToken(token);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse me(RequestUser requestUser) {
        UserAccount account = userAccountRepository.findById(requestUser.userId())
                .orElseThrow(() -> new BusinessException("用户不存在", HttpStatus.UNAUTHORIZED));
        return toUserResponse(account);
    }

    private AuthResponse issueToken(UserAccount account) {
        String tokenValue = (UUID.randomUUID().toString() + UUID.randomUUID())
                .replace("-", "");
        authTokenRepository.save(AuthToken.builder()
                .token(tokenValue)
                .user(account)
                .expiresAt(LocalDateTime.now().plusHours(tokenTtlHours))
                .build());
        return new AuthResponse(tokenValue, toUserResponse(account));
    }

    private void verifyRegistrationCode(String suppliedCode) {
        if (!StringUtils.hasText(teacherRegistrationCode)) {
            throw new BusinessException(
                    "教师公开注册未开放，请联系管理员", HttpStatus.FORBIDDEN);
        }
        if (!StringUtils.hasText(suppliedCode)
                || !MessageDigest.isEqual(
                        suppliedCode.trim().getBytes(StandardCharsets.UTF_8),
                        teacherRegistrationCode.getBytes(StandardCharsets.UTF_8))) {
            throw new BusinessException("教师注册邀请码无效", HttpStatus.FORBIDDEN);
        }
    }

    public UserResponse toUserResponse(UserAccount account) {
        return new UserResponse(
                account.getId(),
                account.getUsername(),
                account.getDisplayName(),
                account.getRole(),
                account.getRole().getLabel(),
                account.getTeacher() == null ? null : account.getTeacher().getId(),
                account.getTeacher() == null ? null : account.getTeacher().getEmployeeNo(),
                account.getTeacher() == null ? null : account.getTeacher().getName());
    }
}
