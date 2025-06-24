package com.greenhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling

public class RegistroInvernaderoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistroInvernaderoApplication.class, args);
    }
}
