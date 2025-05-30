package com.learnkafka.jpa;

import com.learnkafka.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {

    // Additional query methods can be defined here if needed
    // For example, to find by libraryEventId:
    // Optional<LibraryEvent> findByLibraryEventId(Integer libraryEventId);
}
