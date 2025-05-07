package com.karoldm.bookstore.services;


import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.dto.requests.RequestBookDTO;
import com.karoldm.bookstore.dto.requests.UpdateBookAvailableDTO;
import com.karoldm.bookstore.dto.responses.ResponseBookDTO;
import com.karoldm.bookstore.entities.Book;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.exceptions.BookNotFoundException;
import com.karoldm.bookstore.exceptions.StoreNotFoundException;
import com.karoldm.bookstore.mocks.BooksMock;
import com.karoldm.bookstore.repositories.BookRepository;
import com.karoldm.bookstore.repositories.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private BookService bookService;

    private Long storeId;
    private Long bookId;
    private Store store;
    private Book book;
    private RequestBookDTO requestBookDTO;
    private UpdateBookAvailableDTO updateBookAvailableDTO;

    @BeforeEach
    void setup() {
        updateBookAvailableDTO = UpdateBookAvailableDTO.builder()
                .available(true)
                .build();
        bookId = 1L;
        storeId = 1L;
        store = Store.builder()
                .id(storeId)
                .build();
        requestBookDTO = RequestBookDTO.builder()
                .title("book test")
                .summary("...")
                .cover("")
                .releasedAt(LocalDate.of(2025, 1, 1))
                .rating(4)
                .author("author test")
                .available(false)
                .build();

        book = Book.builder()
                .createdAt(LocalDate.of(2025, 4, 17))
                .title("book test")
                .summary("...")
                .id(bookId)
                .author("author test")
                .cover("")
                .available(false)
                .rating(4)
                .releasedAt(LocalDate.of(2025, 1, 1))
                .store(store)
                .build();
    }

    @Nested
    class FindBooksTest {
        @Test
        void mustListPageableBooks() {
            List<Book> paginatedBooks = BooksMock.books.subList(0, 2);

            Page<Book> bookPage = new PageImpl<>(
                    paginatedBooks,
                    PageRequest.of(0, 2),
                    BooksMock.books.size()
            );

            when(bookRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(bookPage);

            Set<ResponseBookDTO> result = bookService
                    .listAll(storeId, 0, 2, BooksFilterDTO.builder().build());

            assertEquals(2, result.size());
        }
    }

    @Nested
    class CreateBookTests {
        @Test
        void mustThrowNotFoundWhenStoreIdIsIncorrect() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () ->
                    bookService.createBook(storeId, requestBookDTO));

            verify(storeRepository, times(1)).findById(storeId);
            verify(bookRepository, times(0)).save(any(Book.class));

            assertEquals("Loja com id " + storeId + " não encontrada.", ex.getMessage());
        }

        @Test
        void mustCreateBook() {
            when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
            when(bookRepository.save(any(Book.class))).thenReturn(
                    Book.builder().id(bookId).build()
            );

            ResponseBookDTO responseBookDTO = bookService.createBook(storeId, requestBookDTO);

            verify(storeRepository, times(1)).findById(storeId);
            verify(bookRepository, times(1)).save(any(Book.class));

            assertEquals(bookId, responseBookDTO.getId());
            assertEquals(requestBookDTO.getTitle(), responseBookDTO.getTitle());
            assertEquals(requestBookDTO.getSummary(), responseBookDTO.getSummary());
            assertEquals(requestBookDTO.getReleasedAt(), responseBookDTO.getReleasedAt());
            assertEquals(requestBookDTO.getRating(), responseBookDTO.getRating());
            assertEquals(requestBookDTO.isAvailable(), responseBookDTO.isAvailable());
            assertEquals(requestBookDTO.getAuthor(), responseBookDTO.getAuthor());
            assertEquals(requestBookDTO.getCover(), responseBookDTO.getCover());
            assertEquals(LocalDate.now(), responseBookDTO.getCreatedAt());
        }
    }

    @Nested
    class DeleteBookTests {
        @Test
        void mustThrowNotFoundWhenBookIdIsIncorrect() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(BookNotFoundException.class, () ->
                    bookService.deleteBook(bookId));

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(0)).delete(any(Book.class));

            assertEquals("Livro com id " + bookId + " não encontrado.", ex.getMessage());
        }

        @Test
        void mustDeleteBook() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

            bookService.deleteBook(bookId);

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(1)).delete(any(Book.class));
        }
    }

    @Nested
    class UpdateBookTests {
        @Test
        void mustThrowNotFoundWhenBookIdIsIncorrect() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(BookNotFoundException.class, () ->
                    bookService.updateBook(bookId, requestBookDTO));

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(0)).save(any(Book.class));

            assertEquals("Livro com id " + bookId + " não encontrado.", ex.getMessage());
        }

        @Test
        void mustUpdateBook() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
            requestBookDTO.setTitle("updated book");
            requestBookDTO.setSummary("updated summary");
            requestBookDTO.setRating(3);
            requestBookDTO.setCover(null);
            requestBookDTO.setAuthor("updated author");
            requestBookDTO.setReleasedAt(LocalDate.of(1990, 12, 12));

            ResponseBookDTO responseBookDTO = bookService.updateBook(bookId, requestBookDTO);

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(1)).save(any(Book.class));

            assertEquals(requestBookDTO.getTitle(), responseBookDTO.getTitle());
            assertEquals(requestBookDTO.getSummary(), responseBookDTO.getSummary());
            assertEquals(requestBookDTO.getReleasedAt(), responseBookDTO.getReleasedAt());
            assertEquals(requestBookDTO.getRating(), responseBookDTO.getRating());
            assertEquals(requestBookDTO.isAvailable(), responseBookDTO.isAvailable());
            assertEquals(requestBookDTO.getAuthor(), responseBookDTO.getAuthor());
            assertEquals(requestBookDTO.getCover(), responseBookDTO.getCover());

            assertEquals(book.getCreatedAt(), responseBookDTO.getCreatedAt());
            assertEquals(book.getId(), responseBookDTO.getId());
        }
    }

    @Nested
    class UpdateAvailableTests {
        @Test
        void mustThrowNotFoundWhenBookIdIsIncorrect() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(BookNotFoundException.class, () ->
                    bookService.changeAvailable(bookId, updateBookAvailableDTO));

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(0)).save(any(Book.class));

            assertEquals("Livro com id " + bookId + " não encontrado.", ex.getMessage());
        }

        @Test
        void mustChangeAvailableBook() {
            when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

            ResponseBookDTO responseBookDTO = bookService.changeAvailable(bookId, updateBookAvailableDTO);

            verify(bookRepository, times(1)).findById(bookId);
            verify(bookRepository, times(1)).save(any(Book.class));

            assertEquals(book.getTitle(), responseBookDTO.getTitle());
            assertEquals(book.getSummary(), responseBookDTO.getSummary());
            assertEquals(book.getReleasedAt(), responseBookDTO.getReleasedAt());
            assertEquals(book.getRating(), responseBookDTO.getRating());
            assertEquals(updateBookAvailableDTO.getAvailable(), responseBookDTO.isAvailable());
            assertEquals(book.getAuthor(), responseBookDTO.getAuthor());
            assertEquals(book.getCover(), responseBookDTO.getCover());

            assertEquals(book.getCreatedAt(), responseBookDTO.getCreatedAt());
            assertEquals(book.getId(), responseBookDTO.getId());
        }
    }
}
