package com.loveofteach.platform.service;

import com.loveofteach.platform.common.BusinessException;
import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.CourseOffering;
import com.loveofteach.platform.security.RequestUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthorizationService {

    public void requireAnyRole(RequestUser user, UserRole... roles) {
        if (Arrays.stream(roles).noneMatch(role -> role == user.role())) {
            throw new BusinessException("当前角色无权执行此操作", HttpStatus.FORBIDDEN);
        }
    }

    public void requireTeacherOwns(RequestUser user, CourseOffering offering) {
        requireAnyRole(user, UserRole.TEACHER);
        if (user.teacherId() == null || !user.teacherId().equals(offering.getTeacher().getId())) {
            throw new BusinessException("只能管理本人所授课程", HttpStatus.FORBIDDEN);
        }
    }

    public void requireTeacherProfile(RequestUser user) {
        requireAnyRole(user, UserRole.TEACHER);
        if (user.teacherId() == null) {
            throw new BusinessException("教师账号未关联教师档案", HttpStatus.FORBIDDEN);
        }
    }
}
