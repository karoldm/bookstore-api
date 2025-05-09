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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookService {
    private BookRepository bookRepository;
    private StoreRepository storeRepository;
    private FileStorageService fileStorageService;

    @Transactional
    public ResponseBookDTO changeAvailable(Long bookId, UpdateBookAvailableDTO updateBookAvailableDTO) {
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
    public void deleteBook(Long bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        if (optionalBook.isEmpty()) {
            throw new BookNotFoundException(bookId);
        }

        Book book = optionalBook.get();

        fileStorageService.removeFileByUrl(book.getCover());

        bookRepository.delete(book);
    }

    @Transactional
    public ResponseBookDTO createBook(Long storeId, RequestBookDTO requestBookDTO) {
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
                .createdAt(LocalDate.now())
                .store(store)
                .rating(requestBookDTO.getRating())
                .build();

        if(requestBookDTO.getCover() != null) {
            String url = fileStorageService.uploadFile(requestBookDTO.getCover());
            book.setCover(url);
        }

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
    public ResponseBookDTO updateBook(Long bookId, RequestBookDTO requestBookDTO) {
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
        book.setReleasedAt(requestBookDTO.getReleasedAt());

        if(requestBookDTO.getCover() != null) {
            if(book.getCover() != null){
                fileStorageService.removeFileByUrl(book.getCover());
            }
            String url = fileStorageService.uploadFile(requestBookDTO.getCover());
            book.setCover(url);
        }

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
            Long storeId,
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
