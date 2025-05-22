package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.LoginRequestDTO;
import com.karoldm.bookstore.dto.requests.RefreshTokenDTO;
import com.karoldm.bookstore.dto.requests.RegisterStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseAuthDTO;
import com.karoldm.bookstore.dto.responses.ResponseRefreshTokenDTO;
import com.karoldm.bookstore.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/auth/")
public class AuthController {
    private AuthService authService;

    @PostMapping("/refresh")
    @Operation(
            summary = "refresh tokens",
            description = "return a new token when refresh-token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "generated successfully"),
            @ApiResponse(responseCode = "401", description = "refresh token is invalid or expired",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    public ResponseEntity<ResponseRefreshTokenDTO> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        ResponseRefreshTokenDTO responseRefreshTokenDTO = authService.refreshToken(refreshTokenDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseRefreshTokenDTO);
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "register a new store account",
            description = "register new store and, consequently, new admin user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created successfully"),
            @ApiResponse(responseCode = "409", description = "username or sotre name already exist"),
            @ApiResponse(responseCode = "400", description = "password with less than 6 char or name, username or slogan blank",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<ResponseAuthDTO> register(@ModelAttribute @Valid RegisterStoreDTO registerDTO) throws Exception {
        ResponseAuthDTO response = authService.register(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "login on app",
            description = "login on app with username and password, must return the token, user and store information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "logged successfully"),
            @ApiResponse(responseCode = "404", description = "username not found"),
            @ApiResponse(responseCode = "401", description = "password incorrect"),
            @ApiResponse(responseCode = "400", description = "password or username is blank",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<ResponseAuthDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) throws Exception {
        ResponseAuthDTO response = authService.login(loginRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
