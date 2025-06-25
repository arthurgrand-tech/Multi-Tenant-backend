package com.ArthurGrand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.SpringServletContainerInitializer;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class TimesheetApplication extends SpringServletContainerInitializer {

    public static void main(String[] args) {
        SpringApplication.run(TimesheetApplication.class, args);
    }

}