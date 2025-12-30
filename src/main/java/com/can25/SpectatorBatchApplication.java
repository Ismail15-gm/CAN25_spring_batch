package com.can25;

import com.can25.Batch.BatchJobRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
//@EnableBatchProcessing  //
public class SpectatorBatchApplication {

    private final BatchJobRunner batchJobRunner;
    public SpectatorBatchApplication(BatchJobRunner batchRunner) {
       this.batchJobRunner = batchRunner;
   }

   @Bean
   CommandLineRunner commandLineRunner() {
       return args -> batchJobRunner.run();
   }

    public static void main(String[] args) {
        SpringApplication.run(SpectatorBatchApplication.class, args);
    }
}