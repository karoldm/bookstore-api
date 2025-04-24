package com.karoldm.bookstore.exceptions;

public class StoreAlreadyExist extends RuntimeException {
    public StoreAlreadyExist(String store) {
        super("Uma loja com o nome " + store + " já existe.");
    }
}
