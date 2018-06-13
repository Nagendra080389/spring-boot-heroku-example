package org.exampledriven.rabbitMQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    Logger logger = LoggerFactory.getLogger(RabbitMQListener.class);

    public void receiveMessage(BigOpertaion bigOpertaion){
        logger.info(bigOpertaion.getAccessToken());
    }
}
