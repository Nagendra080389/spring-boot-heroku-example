package org.exampledriven.rabbitMQ;
import org.exampledriven.SpringBootHerokuExampleApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    @RabbitListener(queues = SpringBootHerokuExampleApplication.PDF_MERGE_QUEUE)
    public void receiveMessage(BigOpertaion bigOpertaion){
        logger.info(" QueueRecieved ======> "+bigOpertaion.getAccessToken());
    }
}
