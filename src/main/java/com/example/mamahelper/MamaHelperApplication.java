package com.example.mamahelper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MamaHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(MamaHelperApplication.class, args);
    }

}
