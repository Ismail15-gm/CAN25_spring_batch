package com.can25.Batch;

import com.can25.Dto.SpectatorDTO;
import com.can25.Entity.SeatLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BatchConfig {

        @Value("${source.type:json}")
        private String sourceType;

        @Bean
        public JsonItemReader<SpectatorDTO> jsonItemReader() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                JacksonJsonObjectReader<SpectatorDTO> jsonObjectReader = new JacksonJsonObjectReader<>(
                                SpectatorDTO.class);
                jsonObjectReader.setMapper(objectMapper);

                return new JsonItemReaderBuilder<SpectatorDTO>()
                                .name("jsonSpectatorReader")
                                .jsonObjectReader(jsonObjectReader)
                                .resource(new ClassPathResource("spectators_json.json"))
                                .strict(false)
                                .build();
        }

        @Bean
        public StaxEventItemReader<SpectatorDTO> xmlItemReader() {
                Map<String, Class<?>> aliases = new HashMap<>();
                aliases.put("spectatorEntry", SpectatorDTO.class);
                aliases.put("seatLocation", SeatLocation.class);

                XStreamMarshaller marshaller = new XStreamMarshaller();
                marshaller.setAliases(aliases);

                // Allow security for XStream
                marshaller.getXStream().allowTypes(new Class[] { SpectatorDTO.class, SeatLocation.class });

                return new StaxEventItemReaderBuilder<SpectatorDTO>()
                                .name("xmlSpectatorReader")
                                .resource(new ClassPathResource("spectators_xml.xml"))
                                .addFragmentRootElements("spectatorEntry")
                                .unmarshaller(marshaller)
                                .build();
        }

        @Bean
        public MainItemReader mainItemReader(
                        @Qualifier("jsonItemReader") ItemReader<SpectatorDTO> jsonReader,
                        @Qualifier("xmlItemReader") ItemReader<SpectatorDTO> xmlReader) {
                return new MainItemReader(jsonReader, xmlReader, sourceType);
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
