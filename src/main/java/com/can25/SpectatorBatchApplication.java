package com.can25;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

@SpringBootApplication
// @EnableBatchProcessing: This annotation triggers Spring Batch's
// auto-configuration.
// It sets up the JobLauncher, JobRepository, and other infrastructure beans
// needed to run jobs.
@EnableBatchProcessing
public class SpectatorBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpectatorBatchApplication.class, args);
    }
}
/**What @EnableBatchProcessing does for you

 When you add:
 @EnableBatchProcessing
 Spring automatically registers core Spring Batch infrastructure beans, such as:

 JobRepository
 JobLauncher
 JobExplorer
 PlatformTransactionManager
 StepBuilderFactory
 JobBuilderFactory

 These are mandatory for running Spring Batch jobs.
 */