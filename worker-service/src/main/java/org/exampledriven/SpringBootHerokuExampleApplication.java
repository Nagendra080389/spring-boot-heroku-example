package org.exampledriven;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootHerokuExampleApplication {
    public final static String PDF_MERGE_QUEUE= "pdf-merge-queue";
    public static void main(String[] args) {
        SpringApplication.run(SpringBootHerokuExampleApplication.class, args);
    }
}
