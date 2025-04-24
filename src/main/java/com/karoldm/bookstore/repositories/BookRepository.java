package com.karoldm.bookstore.repositories;

import com.karoldm.bookstore.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
}
