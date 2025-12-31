package com.can25.Batch;

import com.can25.Dto.SpectatorDTO;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

        @Value("${input.file}")
        private String inputFile;

        @Bean
        public MainItemReader mainItemReader() {
                return new MainItemReader(new ClassPathResource(inputFile));
        }

        @Bean
        public Step spectatorStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        MainItemReader reader,
                        ItemProcessor<SpectatorDTO, SpectatorDTO> processor,
                        DatabaseWriter writer) {

                return new StepBuilder("spectatorStep", jobRepository)
                                .<SpectatorDTO, SpectatorDTO>chunk(10, transactionManager)
                                .reader(reader)
                                .processor(processor)
                                .writer(writer)
                                .faultTolerant()
                                .skip(Exception.class)
                                .skipLimit(5)
                                .build();
        }

        @Bean
        public Job spectatorJob(JobRepository jobRepository, Step spectatorStep) {
                return new JobBuilder("spectatorJob", jobRepository)
                                .incrementer(new RunIdIncrementer())
                                .start(spectatorStep)
                                .build();
        }
}
