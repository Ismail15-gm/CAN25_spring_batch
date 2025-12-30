package com.can25;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
@EnableBatchProcessing
public class SpectatorBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpectatorBatchApplication.class, args);
    }
}