package com.sothawo.taboo2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class Taboo2Application {

    public static void main(String[] args) {
        SpringApplication.run(Taboo2Application.class, args);
    }
}
