package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventsProducer {

    @Value("${spring.kafka.topic}")
    private String topic;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<Integer,String> kafkaTemplate;

    public LibraryEventsProducer(ObjectMapper objectMapper, KafkaTemplate<Integer, String> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key= libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        // 1.blocking call it collects metadata of the kafka cluster
        // 2.send message happens- Returns a completable future
        //this is asynchronous approach
        //this it the one type to send message to topic
        var completableFuture = kafkaTemplate.send(topic,key,value);   //send method return completable future that complete in future

       return completableFuture
                .whenComplete(
                        (sendResult, throwable) -> {
                            if (throwable != null) {
                                //handle the error
                                handleFailure(key,value,throwable);
                            } else {
                                //send success
                                handleSuccess(key , value, sendResult);
                            }
                        }
                );
    }


//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public SendResult<Integer, String> sendLibraryEvent_approach2(LibraryEvent libraryEvent) throws ExecutionException, InterruptedException, JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        // 2nd approach to send message to topic
        // this is synchronous call block and wait until the message is send to the topic.
        var sendResult= kafkaTemplate.send(topic, key, value).get();
        handleSuccess(key,value,sendResult);
        return sendResult;
    }

 //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent_approach3(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key= libraryEvent.libraryEventId();
        var value = objectMapper.writeValueAsString(libraryEvent);

        var producerRecord= buildProducerRecord(key,value);

        // 1.blocking call it collects metadata of the kafka cluster
        // 2.send message happens- Returns a completable future
        //this is asynchronous approach
        //this it the one type to send message to topic
        var completableFuture = kafkaTemplate.send(producerRecord);   //send method return completable future that complete in future

       return completableFuture
                .whenComplete(
                        (sendResult, throwable) -> {
                            if (throwable != null) {
                                //handle the error
                                handleFailure(key,value,throwable);
                            } else {
                                //send success
                                handleSuccess(key , value, sendResult);
                            }
                        }
                );
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {

        List<Header> recordHeaders =List.of(new RecordHeader("event-source", "scanner".getBytes()));
        // use below constructor with headers
        // return new ProducerRecord<>(topic,key,value);
        return new ProducerRecord<>(topic,null, key,value,recordHeaders);

    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message sent successfully for the key: {} and the value is {} and the partition is {}",key,value,sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
    log.error("Error sending the message and the exception is {}",throwable.getMessage());

    }
}
