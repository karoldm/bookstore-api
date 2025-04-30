package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.dto.requests.RequestBookDTO;
import com.karoldm.bookstore.dto.requests.UpdateBookAvailableDTO;
import com.karoldm.bookstore.dto.responses.ResponseBookDTO;
import com.karoldm.bookstore.services.BookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/v1/store/{storeId}/book")
@AllArgsConstructor
public class BookController {
    private BookService bookService;

    @PostMapping
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseBookDTO> createBook(
            @PathVariable UUID storeId,
            @RequestBody @Valid RequestBookDTO requestBookDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseBookDTO responseBookDTO = bookService.createBook(storeId, requestBookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBookDTO);
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseBookDTO> deleteBook(
            @PathVariable UUID storeId,
            @PathVariable UUID bookId,
            @AuthenticationPrincipal Object principal
    ) {
        bookService.deleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{bookId}")
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseBookDTO> updateBook(
            @PathVariable UUID storeId,
            @PathVariable UUID bookId,
            @RequestBody @Valid RequestBookDTO requestBookDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseBookDTO responseBookDTO = bookService.updateBook(bookId, requestBookDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseBookDTO);
    }

    @GetMapping
    @PreAuthorize("@storeSecurityService.canAccessStore(principal, #storeId)")
    ResponseEntity<Set<ResponseBookDTO>> listAllBooks(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            BooksFilterDTO booksFilterDTO,
            @AuthenticationPrincipal Object principal
    ) {
        Set<ResponseBookDTO> listResponseBookDTO = bookService
                .listAll(storeId, page, size, booksFilterDTO);
        return ResponseEntity.status(HttpStatus.OK).body(listResponseBookDTO);
    }

    @PutMapping("/{bookId}/available")
    @PreAuthorize("@storeSecurityService.canAccessStore(principal, #storeId)")
    ResponseEntity<ResponseBookDTO> changeAvailable(
            @PathVariable UUID storeId,
            @PathVariable UUID bookId,
            @RequestBody @Valid UpdateBookAvailableDTO updateBookAvailableDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseBookDTO responseBookDTO = bookService
                .changeAvailable(bookId, updateBookAvailableDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseBookDTO);
    }
}
