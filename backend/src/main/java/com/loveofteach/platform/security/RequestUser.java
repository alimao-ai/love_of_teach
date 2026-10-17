package com.loveofteach.platform.security;

import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.UserAccount;

public record RequestUser(
        Long userId,
        String username,
        String displayName,
        UserRole role,
        Long teacherId
) {

    public static RequestUser from(UserAccount account) {
        return new RequestUser(
                account.getId(),
                account.getUsername(),
                account.getDisplayName(),
                account.getRole(),
                account.getTeacher() == null ? null : account.getTeacher().getId());
    }
}
