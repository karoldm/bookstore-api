package com.karoldm.bookstore.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppStatus {

    @GetMapping("/appstatus")
    @Operation(
            summary = "Status of server",
            description = "Log the server status, if it is running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "server running"),
            @ApiResponse(responseCode = "503", description = "server unavailable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<String> appStatus() {
        return ResponseEntity.status(HttpStatus.OK).body("Server running!");
    }
}
