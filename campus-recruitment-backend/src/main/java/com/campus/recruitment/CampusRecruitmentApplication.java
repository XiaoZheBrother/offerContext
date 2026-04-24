package com.campus.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CampusRecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusRecruitmentApplication.class, args);
    }
}
