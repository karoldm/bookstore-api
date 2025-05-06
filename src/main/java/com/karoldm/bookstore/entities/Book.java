package com.karoldm.bookstore.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;

    @Column
    private String title;
    @Column
    private String summary;
    @Column
    private LocalDate releasedAt;
    @Column
    private String author;
    @Column
    private boolean available;
    @Column
    private int rating;
    @Column
    private String cover;
    @Column
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

}
