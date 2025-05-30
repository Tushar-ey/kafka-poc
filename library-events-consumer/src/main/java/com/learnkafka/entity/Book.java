package com.learnkafka.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Book {

    @Id
    private Integer bookId;

    private String bookName;
    private String bookAuthor;

    @OneToOne
    @JoinColumn(name="libraryEventId")
    private LibraryEvent libraryEvent;
}
