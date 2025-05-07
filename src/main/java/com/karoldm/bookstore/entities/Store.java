package com.karoldm.bookstore.entities;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String name;
    @Column
    private String slogan;
    @Column(columnDefinition = "TEXT")
    private String banner; // base64 image
}
