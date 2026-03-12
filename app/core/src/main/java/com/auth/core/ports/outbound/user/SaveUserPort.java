package com.auth.core.ports.outbound.user;

import com.auth.core.domain.User;

@FunctionalInterface
public interface SaveUserPort {

    User save(User user);
}
