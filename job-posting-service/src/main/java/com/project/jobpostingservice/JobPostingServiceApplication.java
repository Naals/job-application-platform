package com.project.jobpostingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JobPostingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobPostingServiceApplication.class, args);
    }

}
