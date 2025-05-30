package com.learnkafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component    // we make this comment because we are using Manual Offset management in Kafka
@Slf4j
public class LibraryEventsConsumer {

    @Autowired
     private LibraryEventsService libraryEventsService;

        @KafkaListener(topics = "library-events")
        public void onMessage(ConsumerRecord<Integer,String> consumerRecord) throws JsonProcessingException {
            // This method will be called when a message is received from the Kafka topic
            log.info("Received a message from the Kafka topic , consumerRecord: {}", consumerRecord);
            // Here you can add logic to process the message, such as deserializing it,
            // validating it, and performing any necessary business logic.
            libraryEventsService.processLibraryEvent(consumerRecord);
        }
}
