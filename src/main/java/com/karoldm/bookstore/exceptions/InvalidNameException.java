package com.karoldm.bookstore.exceptions;

public class InvalidNameException extends RuntimeException {
    public InvalidNameException(String name){
        super(name+" não é um nome válido. Insira ao menos 1 caractere.");
    }
}
