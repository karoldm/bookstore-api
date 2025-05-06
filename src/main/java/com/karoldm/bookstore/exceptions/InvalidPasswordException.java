package com.karoldm.bookstore.exceptions;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(){
        super("A senha deve possuir pelo menos 6 caracteres.");
    }
}
