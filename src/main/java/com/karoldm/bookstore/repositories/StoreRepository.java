package com.karoldm.bookstore.repositories;

import com.karoldm.bookstore.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByName(String name);
}
