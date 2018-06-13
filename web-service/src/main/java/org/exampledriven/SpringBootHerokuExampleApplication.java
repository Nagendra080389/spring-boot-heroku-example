package org.exampledriven;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootHerokuExampleApplication {

    public final static String PDF_MERGE_QUEUE= "pdf-merge-queue";
    public static final String PDF_SPLIT_QUEUE = "pdf-split-queue";



    public static void main(String[] args) {
        SpringApplication.run(SpringBootHerokuExampleApplication.class, args);
    }
}
