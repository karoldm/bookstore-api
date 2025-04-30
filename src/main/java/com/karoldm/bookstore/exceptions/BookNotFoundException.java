package com.karoldm.bookstore.exceptions;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID id){
        super("Livro com id " + id + " não encontrado." );
    }
}
