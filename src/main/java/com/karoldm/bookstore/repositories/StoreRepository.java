package com.karoldm.bookstore.repositories;

import com.karoldm.bookstore.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByName(String name);
}
