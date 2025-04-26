package com.karoldm.bookstore.exceptions;

public class UnauthorizedStoreOperationException extends RuntimeException {
    public UnauthorizedStoreOperationException(){
        super("Você não tem acesso a esse recurso.");
    }
}
