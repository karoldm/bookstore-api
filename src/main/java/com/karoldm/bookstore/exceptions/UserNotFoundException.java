package com.karoldm.bookstore.exceptions;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String username) {
        super("Usuário com username " + username + " não encontrado.");
    }

    public UserNotFoundException(UUID id) {
        super("Usuário com ID " + id + " não encontrado.");
    }
}
