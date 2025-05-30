package com.learnkafka.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LibraryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer libraryEventId;
    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;

    @OneToOne(mappedBy = "libraryEvent",cascade= CascadeType.ALL)   //here cascade is used to persist the book when the library event is persisted
   @ToString.Exclude // Exclude book from toString to avoid circular reference and out of Memory error
    private Book book;
}
