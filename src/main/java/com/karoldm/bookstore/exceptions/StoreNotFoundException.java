package com.karoldm.bookstore.exceptions;

import java.util.UUID;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(UUID id) {
        super("Loja com id " + id + " não encontrada.");
    }
}
