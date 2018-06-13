package org.exampledriven;

import org.exampledriven.rabbitMQ.RabbitMQListener;
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

    @Bean
    Queue queue1(){
        return new Queue(PDF_MERGE_QUEUE, false);
    }

    @Bean
    Queue queue2(){
        return new Queue(PDF_SPLIT_QUEUE, false);
    }

    @Bean
    TopicExchange exchange1(){
        return new TopicExchange("pdf-merge-exchange");
    }

    @Bean
    TopicExchange exchange2(){
        return new TopicExchange("pdf-split-exchange");
    }

    @Bean
    Binding binding1(Queue queue1, TopicExchange topicExchange1){
        return BindingBuilder.bind(queue1).to(topicExchange1).with(PDF_MERGE_QUEUE);
    }

    @Bean
    Binding binding2(Queue queue2, TopicExchange topicExchange3){
        return BindingBuilder.bind(queue2).to(topicExchange3).with(PDF_SPLIT_QUEUE);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(PDF_MERGE_QUEUE, PDF_SPLIT_QUEUE);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter1(RabbitMQListener rabbitMQListener){
        return new MessageListenerAdapter(rabbitMQListener, "mergeProcess");
    }

    @Bean
    MessageListenerAdapter listenerAdapter2(RabbitMQListener rabbitMQListener){
        return new MessageListenerAdapter(rabbitMQListener, "splitProcess");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootHerokuExampleApplication.class, args);
    }
}
