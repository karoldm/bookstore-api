package com.karoldm.bookstore.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String username) {
        super("Usuário com username " + username + " não encontrado.");
    }
}
