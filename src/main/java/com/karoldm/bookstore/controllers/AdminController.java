package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@AllArgsConstructor
public class AdminController {
    private AdminService adminService;

    @PutMapping()
    @Operation(
            summary = "update admin account",
            description = "Allow an admin update your own account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated successfully"),
            @ApiResponse(responseCode = "403", description = "user not authenticated or user does not have role permission"),
            @ApiResponse(responseCode = "400", description = "password with less than 6 char or name blank",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<ResponseUserDTO> updateAccount(
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = adminService.updateAccount((AppUser) principal, updateUserDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseUserDTO);
    }

    @DeleteMapping()
    @Operation(
            summary = "delete admin account",
            description = "Allow an admin delete your own account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "deleted successfully"),
            @ApiResponse(responseCode = "403", description = "user not authenticated or user does not have role permission",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal Object principal
    ) {
        adminService.deleteAccount((AppUser) principal);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
