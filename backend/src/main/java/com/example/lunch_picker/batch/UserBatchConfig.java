package com.example.lunch_picker.batch;

import com.example.lunch_picker.model.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class UserBatchConfig {

    private final EntityManagerFactory emf;
    private final JobLauncher jobLauncher;

    @Value("${app.users.csv-path}")
    private String usersCsvPath;


    @Bean
    public FlatFileItemReader<User> userReader() {
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(usersCsvPath));
        reader.setLinesToSkip(1);
        reader.setLineMapper((line, lineNumber) -> new User(line.trim()));
        return reader;
    }


    @Bean
    public JpaItemWriter<User> userWriter() {
        JpaItemWriter<User> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }


    @Bean
    public Step loadUsersStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              FlatFileItemReader<User> reader,
                              JpaItemWriter<User> writer) {
        return new StepBuilder("loadUsersStep", jobRepository)
                .<User, User>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }


    @Bean
    public Job loadUsersJob(JobRepository jobRepository,
                            Step loadUsersStep) {
        return new JobBuilder("loadUsersJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(loadUsersStep)
                .build();
    }

    @Bean
    public CommandLineRunner runBatchJob(Job loadUsersJob) {
        return args -> jobLauncher.run(loadUsersJob, new JobParameters());
    }
}