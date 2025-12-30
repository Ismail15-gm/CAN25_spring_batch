package com.can25.Batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;

import org.springframework.stereotype.Service;

@Service("batchJobRunner")
public class BatchJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job spectatorJob;

    public BatchJobRunner(JobLauncher jobLauncher, Job spectatorJob) {
        this.jobLauncher = jobLauncher;
        this.spectatorJob = spectatorJob;
    }

    @Override
    public void run(String... args) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("fileType", "json")
                .toJobParameters();

        jobLauncher.run(spectatorJob, jobParameters);

    }
}