package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.dto.requests.RequestBookDTO;
import com.karoldm.bookstore.dto.requests.UpdateBookAvailableDTO;
import com.karoldm.bookstore.dto.responses.ResponseBookDTO;
import com.karoldm.bookstore.entities.Book;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.exceptions.BookNotFoundException;
import com.karoldm.bookstore.exceptions.StoreNotFoundException;
import com.karoldm.bookstore.filters.BooksFilters;
import com.karoldm.bookstore.repositories.BookRepository;
import com.karoldm.bookstore.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookService {
    private BookRepository bookRepository;
    private StoreRepository storeRepository;

    @Transactional
    public ResponseBookDTO changeAvailable(UUID bookId, UpdateBookAvailableDTO updateBookAvailableDTO) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isEmpty()) {
            throw new BookNotFoundException(bookId);
        }

        Book book = optionalBook.get();

        book.setAvailable(updateBookAvailableDTO.getAvailable());

        bookRepository.save(book);

        return ResponseBookDTO.builder()
                .id(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .summary(book.getSummary())
                .releasedAt(book.getReleasedAt())
                .available(book.isAvailable())
                .cover(book.getCover())
                .createdAt(book.getCreatedAt())
                .rating(book.getRating())
                .build();
    }

    @Transactional
    public void deleteBook(UUID bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isEmpty()) {
            throw new BookNotFoundException(bookId);
        }

        Book book = optionalBook.get();

        bookRepository.delete(book);
    }

    @Transactional
    public ResponseBookDTO createBook(UUID storeId, RequestBookDTO requestBookDTO) {
        Optional<Store> optionalStore = storeRepository.findById(storeId);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(storeId);
        }

        Store store = optionalStore.get();

        Book book = Book.builder()
                .author(requestBookDTO.getAuthor())
                .title(requestBookDTO.getTitle())
                .summary(requestBookDTO.getSummary())
                .releasedAt(requestBookDTO.getReleasedAt())
                .available(requestBookDTO.isAvailable())
                .cover(requestBookDTO.getCover())
                .createdAt(LocalDate.now())
                .store(store)
                .rating(requestBookDTO.getRating())
                .build();

        Book savedBook = bookRepository.save(book);
        book.setId(savedBook.getId());

        return ResponseBookDTO.builder()
                .id(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .summary(book.getSummary())
                .releasedAt(book.getReleasedAt())
                .available(book.isAvailable())
                .cover(book.getCover())
                .rating(book.getRating())
                .createdAt(book.getCreatedAt())
                .build();
    }

    @Transactional
    public ResponseBookDTO updateBook(UUID bookId, RequestBookDTO requestBookDTO) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isEmpty()) {
            throw new BookNotFoundException(bookId);
        }

        Book book = optionalBook.get();

        book.setAuthor(requestBookDTO.getAuthor());
        book.setTitle(requestBookDTO.getTitle());
        book.setRating(requestBookDTO.getRating());
        book.setSummary(requestBookDTO.getSummary());
        book.setAvailable(requestBookDTO.isAvailable());
        book.setCover(requestBookDTO.getCover());
        book.setReleasedAt(requestBookDTO.getReleasedAt());

        bookRepository.save(book);

        return ResponseBookDTO.builder()
                .id(book.getId())
                .author(book.getAuthor())
                .title(book.getTitle())
                .summary(book.getSummary())
                .releasedAt(book.getReleasedAt())
                .available(book.isAvailable())
                .cover(book.getCover())
                .rating(book.getRating())
                .createdAt(book.getCreatedAt())
                .build();
    }

    public Set<ResponseBookDTO> listAll(
            UUID storeId,
            int page,
            int size,
            BooksFilterDTO booksFilterDTO
    ) {

        Sort sortByCreatedAt = Sort.by("createdAt");
        Pageable pageRequest = PageRequest.of(page, size, sortByCreatedAt);

        BooksFilters booksFilters = new BooksFilters(booksFilterDTO, storeId);

        Page<Book> books = bookRepository.findAll(booksFilters, pageRequest);

        return books.stream()
                .map(book ->
                        ResponseBookDTO.builder()
                                .id(book.getId())
                                .author(book.getAuthor())
                                .rating(book.getRating())
                                .title(book.getTitle())
                                .summary(book.getSummary())
                                .available(book.isAvailable())
                                .releasedAt(book.getReleasedAt())
                                .cover(book.getCover())
                                .createdAt(book.getCreatedAt())
                                .build())
                .collect(Collectors.toSet());
    }
}
