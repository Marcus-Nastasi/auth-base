package com.auth.auth.adapters.inbound.rest;

import com.auth.core.ports.inbound.auth.AuthUseCasePort;
import com.auth.core.ports.inbound.auth.TokenPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(
//    controllers = {
//        JwksController.class,
//        IamController.class
//    },
//    properties = "spring.cache.type=none"
//)
//@AutoConfigureMockMvc(addFilters = false)
abstract class ControllerTestBase {
//
//    @Autowired
//    protected MockMvc mockMvc;
//
//    @MockitoBean
//    protected TokenPort tokenPort;
//
//    @MockitoBean
//    protected AuthUseCasePort authUseCasePort;
//
//    @MockitoBean
//    private org.springframework.cache.CacheManager cacheManager;
}
