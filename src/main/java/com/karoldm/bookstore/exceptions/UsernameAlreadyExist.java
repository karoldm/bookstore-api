package com.karoldm.bookstore.exceptions;

public class UsernameAlreadyExist extends RuntimeException {
    public UsernameAlreadyExist(String username) {
        super("Já existe um usuário com o username " + username);
    }
}
