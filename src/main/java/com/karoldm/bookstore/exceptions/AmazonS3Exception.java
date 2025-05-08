package com.karoldm.bookstore.exceptions;

public class AmazonS3Exception extends RuntimeException {
    public AmazonS3Exception(String message) {
        super(message);
    }
}
