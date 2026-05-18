package com.auth.user.adapters.outbound.repository.config;

import com.auth.user.adapters.outbound.repository.UserJpaRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public abstract class JpaDatabaseTestBase {

    @Autowired
    protected UserJpaRepo userJpaRepo;

    @PersistenceContext
    protected EntityManager entityManager;

}
