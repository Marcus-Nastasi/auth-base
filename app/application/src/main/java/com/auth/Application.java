package com.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(scanBasePackages = {
        "com.auth",
        "com.auth.auth",
        "com.auth.core",
        "com.auth.user"
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
