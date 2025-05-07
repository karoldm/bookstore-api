package com.karoldm.bookstore.exceptions;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(Long id) {
        super("Loja com id " + id + " não encontrada.");
    }
}
