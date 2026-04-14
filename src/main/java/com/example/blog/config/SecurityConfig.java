package com.example.blog.config;

import com.example.blog.common.AdminPermission;
import com.example.blog.service.impl.auth.JpaUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JpaUserDetailsService userDetailsService;
  private final com.example.blog.security.LoginSuccessHandler loginSuccessHandler;
  private final com.example.blog.security.LoginFailureHandler loginFailureHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/", "/articles", "/articles/search", "/articles/{slug}", "/articles/{slug}/comments", "/articles/image/**",
                            "/auth/**",
                            "/error", "/error/**",
                            "/favicon.ico",
                            "/css/**", "/js/**", "/images/**",
                            "/actuator/health", "/actuator/health/**"
                    ).permitAll()
                    .requestMatchers("/articles/editor/**", "/articles/editor")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.ARTICLE_WRITE))
                    .requestMatchers(HttpMethod.POST, "/articles/upload-image")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.ARTICLE_WRITE))
                    .requestMatchers(HttpMethod.POST, "/articles/*/delete")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.ARTICLE_MANAGE))
                    .requestMatchers("/admin/users/**")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.USER_MANAGE))
                    .requestMatchers("/admin/stats/**")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.STATS_VIEW))
                    .requestMatchers("/admin/articles/**")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.ARTICLE_MANAGE))
                    .requestMatchers("/admin/comments/**")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.COMMENT_MODERATE))
                    .requestMatchers("/admin/notifications/**")
                    .hasAnyAuthority(authoritiesOf(AdminPermission.NOTIFICATION_MANAGE))
                    .requestMatchers("/admin", "/actuator/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
                .permitAll()
            )
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/error/403")
        )
        .logout(logout -> logout
            .logoutUrl("/auth/logout")
            .logoutSuccessUrl("/")
        );

    return http.build();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private String[] authoritiesOf(AdminPermission... permissions) {
    return Arrays.stream(permissions)
        .map(AdminPermission::getAuthority)
        .toArray(String[]::new);
  }
}
