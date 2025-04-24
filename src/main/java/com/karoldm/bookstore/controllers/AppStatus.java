package com.karoldm.bookstore.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppStatus {
    @GetMapping("/appstatus")
    ResponseEntity<String> appStatus() {
        return ResponseEntity.status(HttpStatus.OK).body("Server running!");
    }
}
