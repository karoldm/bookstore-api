package com.karoldm.bookstore.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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
    @Column(columnDefinition = "TEXT")
    private String cover;
    @Column
    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

}
