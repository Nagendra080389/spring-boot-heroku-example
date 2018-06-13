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

    @Bean
    Queue queue(){
        return new Queue(PDF_MERGE_QUEUE, false);
    }

    @Bean
    TopicExchange exchange(){
        return new TopicExchange("pdf-merge-exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(PDF_MERGE_QUEUE);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter listenerAdapter){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(PDF_MERGE_QUEUE);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(RabbitMQListener rabbitMQListener){
        return new MessageListenerAdapter(rabbitMQListener, "receiveMessage");
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringBootHerokuExampleApplication.class, args);
    }
}
