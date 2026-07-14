package com.auth.auth.adapters.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
         ClientRegistrationController.class
    },
    properties = "spring.cache.type=none"
)
@AutoConfigureMockMvc
abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    protected static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

//    @MockitoBean
//    private org.springframework.cache.CacheManager cacheManager;
}
