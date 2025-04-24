package com.karoldm.bookstore.entities;

import com.karoldm.bookstore.enums.Roles;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
abstract public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String name;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String photo; // base64 image
    @Column
    private Roles role;

}
