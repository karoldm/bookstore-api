package com.karoldm.bookstore.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;
    @Column
    private String name;
    @Column
    private String slogan;
    @Column
    private String banner; // base64 image
}
