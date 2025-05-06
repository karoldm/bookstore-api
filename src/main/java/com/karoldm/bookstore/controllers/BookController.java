package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.dto.requests.RequestBookDTO;
import com.karoldm.bookstore.dto.requests.UpdateBookAvailableDTO;
import com.karoldm.bookstore.dto.responses.ResponseBookDTO;
import com.karoldm.bookstore.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
    @Operation(
            summary = "create new book",
            description = "allow admin create new book on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
    @Operation(
            summary = "delete book",
            description = "allow admin delete a book on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "deleted successfully"),
            @ApiResponse(responseCode = "404", description = "book not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
    @Operation(
            summary = "update book",
            description = "allow admin update a book on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated successfully"),
            @ApiResponse(responseCode = "404", description = "book not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
    @Operation(
            summary = "list books",
            description = "allow admin or employee list all store's book on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "listed successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
    @Operation(
            summary = "update stock",
            description = "allow an employee update a book's stock on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated successfully"),
            @ApiResponse(responseCode = "404", description = "book not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
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
