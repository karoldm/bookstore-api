package com.karoldm.bookstore.repositories;

import com.karoldm.bookstore.entities.Book;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @NotNull Page<Book> findAll(Specification<Book> spec, @NotNull Pageable pageable);
}
