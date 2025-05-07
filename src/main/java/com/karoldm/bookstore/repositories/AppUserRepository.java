package com.karoldm.bookstore.repositories;

import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    Set<AppUser> findByStoreAndRole(Store store, Roles role);

    Optional<AppUser> findByIdAndStoreAndRole(Long id, Store store, Roles role);
}
