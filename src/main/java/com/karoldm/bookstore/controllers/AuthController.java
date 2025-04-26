package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.LoginRequestDTO;
import com.karoldm.bookstore.dto.responses.ResponseAuthDTO;
import com.karoldm.bookstore.dto.requests.RegisterStoreDTO;
import com.karoldm.bookstore.services.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/auth/")
public class AuthController {
    private AuthService authService;

    @PostMapping("/register")
    ResponseEntity<ResponseAuthDTO> register(@RequestBody @Valid RegisterStoreDTO registerDTO) throws Exception {
        ResponseAuthDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    ResponseEntity<ResponseAuthDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) throws Exception {
        ResponseAuthDTO response = authService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
