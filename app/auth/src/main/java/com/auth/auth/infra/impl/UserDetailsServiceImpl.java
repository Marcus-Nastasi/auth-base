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
      final User user = findUserPort.findUserByCpf(cpf).orElseThrow(this::throwNotFound);

      final Set<SimpleGrantedAuthority> authorities =
              Set.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));

      final boolean isActive = user.getInactivatedAt() == null;

      return org.springframework.security.core.userdetails.User.builder()
              .username(user.getId().toString())
              .password(user.getPassword())
              .authorities(authorities)
              .accountExpired(false)
              .accountLocked(!isActive)
              .credentialsExpired(false)
              .disabled(!isActive)
              .build();
   }

   private UsernameNotFoundException throwNotFound() {
      return new UsernameNotFoundException("User not found");
   }
}
