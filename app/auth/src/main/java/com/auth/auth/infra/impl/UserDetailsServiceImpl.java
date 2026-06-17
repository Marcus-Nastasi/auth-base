package com.auth.auth.infra.impl;

import com.auth.core.domain.User;
import com.auth.core.ports.outbound.user.FindUserPort;
import lombok.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

   private final FindUserPort findUserPort;

   public UserDetailsServiceImpl(final FindUserPort findUserPort) {
      this.findUserPort = findUserPort;
   }

   @NonNull
   @Override
   public UserDetails loadUserByUsername(@NonNull final String cpf) throws UsernameNotFoundException {
      final User user = findUserPort.findUserByCpf(cpf)
              .orElseThrow(() -> new UsernameNotFoundException("User not found: " + cpf));

      final Set<SimpleGrantedAuthority> authorities =
              Set.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));

      return org.springframework.security.core.userdetails.User.builder()
              .username(user.getId().toString())
              .password(user.getPassword())
              .authorities(authorities)
              .accountExpired(false)
              .accountLocked(user.getInactivatedAt() != null)
              .credentialsExpired(false)
              .disabled(false)
              .build();
   }
}
