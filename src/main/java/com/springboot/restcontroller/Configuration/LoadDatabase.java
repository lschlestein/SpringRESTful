package com.springboot.restcontroller.Configuration;

import com.springboot.restcontroller.Model.Student;
import com.springboot.restcontroller.Repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(StudentRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Student("John Doe", "Enginering", "john@mail.com.ar")));
            log.info("Preloading " + repository.save(new Student("Jane Doe", "Arts", "jane@mail.com.ar")));
            log.info("Preloading " + repository.save(new Student("Peter Dilan", "History", "dilan@mail.com.ar")));
        };
    }
}
