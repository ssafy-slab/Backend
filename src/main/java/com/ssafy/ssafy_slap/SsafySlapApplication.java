package com.ssafy.ssafy_slap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SsafySlapApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsafySlapApplication.class, args);
    }

}
