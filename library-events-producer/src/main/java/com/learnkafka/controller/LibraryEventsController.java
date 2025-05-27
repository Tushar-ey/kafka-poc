package com.learnkafka.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import com.learnkafka.producer.LibraryEventsProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    private LibraryEventsProducer libraryEventsProducer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("LibraryEvent: {}", libraryEvent);
            //invoke the kafka producer
        libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
        log.info("LibraryEvent sent to Kafka topic");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<?> updateLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("LibraryEvent: {}", libraryEvent);

        ResponseEntity<String> BAD_REQUEST = getStringResponseEntity(libraryEvent);
        if (BAD_REQUEST != null) return BAD_REQUEST;
        //invoke the kafka producer
        libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);
        log.info("LibraryEvent sent to Kafka topic");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    private static ResponseEntity<String> getStringResponseEntity(LibraryEvent libraryEvent) {
        if(libraryEvent.libraryEventId() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the libraryEventId to update the LibraryEvent");
        }

        if(!libraryEvent.libraryEventType().equals(LibraryEventType.UPDATE)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only update interface is supported");
        }
        return null;
    }
}
