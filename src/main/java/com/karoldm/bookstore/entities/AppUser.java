package com.karoldm.bookstore.entities;

import com.karoldm.bookstore.enums.Roles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private UUID id;

    @Column
    private String name;
    @Column(unique = true)
    private String username;
    @Column
    private String password;
    @Column(nullable = false)
    private Roles role;

    @OneToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}
