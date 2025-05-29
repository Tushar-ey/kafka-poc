package com.learnkafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component    // wemake this comment because we are using Manual Offset management in Kafka
@Slf4j
public class LibraryEventsConsumer {

        @KafkaListener(topics = "library-events")
        public void onMessage(ConsumerRecord<Integer,String> consumerRecord) {
            // This method will be called when a message is received from the Kafka topic
            log.info("Received a message from the Kafka topic , consumerRecord: {}", consumerRecord);
            // Here you can add logic to process the message, such as deserializing it,
            // validating it, and performing any necessary business logic.
        }
}
