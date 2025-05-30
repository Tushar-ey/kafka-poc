package com.learnkafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.entity.Book;
import com.learnkafka.entity.LibraryEvent;
import com.learnkafka.entity.LibraryEventType;
import com.learnkafka.jpa.LibraryEventsRepository;
import com.learnkafka.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events", partitions = 3)             // test for embedded kafka  set  topics and partitions
@TestPropertySource(properties = {"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers","" +
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",})
public class LibraryEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;
    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;    //hold of all the listeners containers here LibraryEventsConsumer.java is listener container

   @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;  //spyBean is used to mock the real bean of  LibraryEventsConsumer so that we can verify the interactions with it

  @SpyBean
    LibraryEventsService libraryEventsServiceSpy;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

   // @Autowired
    //FailureRecordRepository failureRecordRepository;

    @Autowired
    ObjectMapper objectMapper;



    @BeforeEach
    void setup(){
      for(MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()){

         // ensure that the MessageListenerContainer is fully assigned to the partitions of the topic before proceeding with the test.
          //means LibraryEventsConsumer is going to wait until all the partitions are assigned to it  so that out test case can run smoothly
          ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
      }
    }

    @AfterEach
    void tearDown() {

        libraryEventsRepository.deleteAll();
        //failureRecordRepository.deleteAll();

    }


    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String json = " {\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        kafkaTemplate.sendDefault(json).get();   //byDefauly sendDefault is asynchronous call, but we are using here .get() to make it synchronous

        //when
        //countDownLatch is used to block the current execution of thread. Really helpful when writing test cases for async calls
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS); // wait for 3 seconds for the latch to be counted down
        verify(libraryEventsConsumerSpy, times(1)).onMessage(any(ConsumerRecord.class)); //verify that the onMessage method of LibraryEventsConsumer is called once
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(any(ConsumerRecord.class));
       List<LibraryEvent> libraryEventList= (List<LibraryEvent>) libraryEventsRepository.findAll();

        assert libraryEventList.size() == 1;
        libraryEventList.forEach(libraryEvent -> {
            assert libraryEvent.getLibraryEventId() != null;
            assertEquals(1001, libraryEvent.getBook().getBookId());
        });
    }

    @Test
    void publishUpdateLibraryEvent() throws JsonProcessingException, InterruptedException, ExecutionException {
        String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);

        Book updatedBook = Book.builder().
                bookId(456).bookName("Kafka Using Spring Boot 2.x").bookAuthor("Dilip").build();
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEvent.setBook(updatedBook);
        String updatedJson = objectMapper.writeValueAsString(libraryEvent);
        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
        LibraryEvent persistedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
        assertEquals("Kafka Using Spring Boot 2.x", persistedLibraryEvent.getBook().getBookName());

    }
}
