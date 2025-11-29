package com.jrm.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(JrmApplication.class, args);
    }
}
