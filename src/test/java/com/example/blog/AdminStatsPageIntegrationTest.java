package com.example.blog;

import com.example.blog.common.AdminPermission;
import com.example.blog.common.Role;
import com.example.blog.entity.ActionLog;
import com.example.blog.entity.User;
import com.example.blog.repository.ActionLogRepository;
import com.example.blog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminStatsPageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ActionLogRepository actionLogRepository;

  @BeforeEach
  void setUp() {
    User admin = userRepository.findByUsername("admin")
        .orElseGet(() -> userRepository.save(User.builder()
            .username("admin")
            .nickname("admin")
            .passwordHash("noop")
            .role(Role.ROLE_ADMIN)
            .createdAt(Instant.now())
            .enabled(true)
            .build()));
    admin.setRole(Role.ROLE_ADMIN);
    admin.setSuperAdmin(false);
    admin.grantAdminPermissions(java.util.Set.of(AdminPermission.STATS_VIEW));
    admin = userRepository.save(admin);

    actionLogRepository.save(ActionLog.builder()
        .user(admin)
        .time(Instant.now())
        .action("测试访问统计页")
        .detail("integration-test")
        .build());
  }

  @Test
  void statsPage_shouldRender() throws Exception {
    mockMvc.perform(get("/admin/stats")
            .with(SecurityMockMvcRequestPostProcessors.user("admin")
                .authorities(new SimpleGrantedAuthority("ADMIN_STATS_VIEW"))))
        .andExpect(status().isOk());
  }
}

