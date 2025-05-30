package com.learnkafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.consumer.LibraryEventsConsumer;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.jpa.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    private LibraryEventsRepository libraryEventsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {

        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("LibraryEvent: {}", libraryEvent);
        //extract libraryEventType from LibraryEvent and write switch expression to handle different event types
        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                // save operation
                add(libraryEvent);
                break;
            case UPDATE:
                // update operation
                validate(libraryEvent);
                add(libraryEvent);
                log.info("update library event: {}", libraryEvent);
                break;
            default:
                log.error("Unknown library event type: {}", libraryEvent.getLibraryEventType());
        }

    }

    private void validate(LibraryEvent libraryEvent) {
        if(libraryEvent.getLibraryEventId()==null){
            throw new IllegalArgumentException("Library Event Id is missing");
        }
       Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if(libraryEventOptional.isEmpty()){
           throw new IllegalArgumentException("Not a Valid library event");
        }
        log .info("Successfully validated library event: {}", libraryEventOptional.get());
    }

    private void add(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully Save new library event: {}", libraryEvent);
    }
}
