package com.swarm.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SwarmServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwarmServerApplication.class, args);
    }

}
