package com.example.blog.service.impl.auth;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
    User u = userRepository.findByUsername(login)
        .or(() -> userRepository.findByEmail(login))
        .or(() -> userRepository.findByPhone(login))
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(),
        u.getPasswordHash(),
        List.of(new SimpleGrantedAuthority(u.getRole().name()))
    );
  }
}
