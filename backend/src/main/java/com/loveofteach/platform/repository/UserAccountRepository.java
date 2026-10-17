package com.loveofteach.platform.repository;

import com.loveofteach.platform.common.UserRole;
import com.loveofteach.platform.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByTeacherId(Long teacherId);

    List<UserAccount> findByRoleOrderByDisplayNameAsc(UserRole role);
}
