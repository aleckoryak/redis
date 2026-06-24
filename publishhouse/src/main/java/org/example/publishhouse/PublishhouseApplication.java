package org.example.publishhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PublishhouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublishhouseApplication.class, args);
    }
}
