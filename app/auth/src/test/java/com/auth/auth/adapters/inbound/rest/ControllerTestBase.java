package com.auth.auth.adapters.inbound.rest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(
    controllers = {
        JwksController.class,
        IamController.class
    },
    properties = "spring.cache.type=none"
)
@AutoConfigureMockMvc(addFilters = false)
class ControllerTestBase {

    @MockitoBean
    private org.springframework.cache.CacheManager cacheManager;
}
