package org.exampledriven.rabbitMQ;

import org.exampledriven.SpringBootHerokuExampleApplication;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void merge(BigOpertaion bigOpertaion){


        rabbitTemplate.convertAndSend(SpringBootHerokuExampleApplication.PDF_MERGE_QUEUE, bigOpertaion);
    }

}
