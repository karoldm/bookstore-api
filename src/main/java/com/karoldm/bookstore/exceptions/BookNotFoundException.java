package com.karoldm.bookstore.exceptions;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id){
        super("Livro com id " + id + " não encontrado." );
    }
}
