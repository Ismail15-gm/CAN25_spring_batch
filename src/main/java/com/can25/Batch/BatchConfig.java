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
// @EnableBatchProcessing is likely in the main application class, enabling
// Spring Batch features.
public class BatchConfig {

        // The input file path is injected from application.properties
        @Value("${input.file}")
        private String inputFile;

        /**
         * DEFINING THE READER BEAN
         * This bean creates an instance of our custom MainItemReader.
         * It is responsible for reading data from the specified input file.
         * We pass the file resource (ClassPathResource) to the reader.
         */
        @Bean
        public MainItemReader mainItemReader() {
                return new MainItemReader(new ClassPathResource(inputFile));
        }

        /**
         * DEFINING THE STEP
         * A Step is a domain object that encapsulates an independent, sequential phase
         * of a batch job.
         * This step follows the standard "Chunk-Oriented Processing" model: Read ->
         * Process -> Write.
         *
         * @param jobRepository      Maintains metadata about the batch job execution
         *                           (status, invalid items, etc.)
         * @param transactionManager Manages transactions for the batch processing.
         * @param reader             Our custom reader (defined above).
         * @param processor          Our custom processor (SpectatorProcessor).
         * @param writer             Our custom writer (DatabaseWriter).
         */
        @Bean
        public Step spectatorStep(
                        JobRepository jobRepository,
                        PlatformTransactionManager transactionManager,
                        MainItemReader reader,
                        ItemProcessor<SpectatorDTO, SpectatorDTO> processor,
                        DatabaseWriter writer) {

                return new StepBuilder("spectatorStep", jobRepository)
                                // <InputType, OutputType>chunk(batchSize, transactionManager)
                                // We process data in chunks of 10. This means:
                                // 1. Read 10 items.
                                // 2. Process those 10 items.
                                // 3. Write those 10 items in a single transaction.
                                // This improves performance compared to processing one by one.
                                .<SpectatorDTO, SpectatorDTO>chunk(10, transactionManager)
                                .reader(reader)
                                .processor(processor)
                                .writer(writer)
                                // Fault Tolerance Configuration
                                // If an error occurs (Exception.class), we skip the bad item instead of
                                // crashing the whole job.
                                // limit(5) means we tolerate up to 5 skipped items before failing the job.
                                .faultTolerant()
                                .skip(Exception.class)
                                .skipLimit(5)
                                .build();
        }

        /**
         * DEFINING THE JOB
         * A Job is an entity that encapsulates an entire batch process.
         * It allows us to define the sequence of steps.
         *
         * @param jobRepository Maintains job execution state.
         * @param spectatorStep The step we defined above.
         */
        @Bean
        public Job spectatorJob(JobRepository jobRepository, Step spectatorStep) {
                return new JobBuilder("spectatorJob", jobRepository)
                                // Adds a unique ID to every run, allowing the job to be restarted or run
                                // multiple times
                                .incrementer(new RunIdIncrementer())
                                // The flow of steps to execute (in this simple case, just one step)
                                .start(spectatorStep)
                                .build();
        }
}
