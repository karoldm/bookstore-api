package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.services.AdminService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin")
@AllArgsConstructor
public class AdminController {
    private AdminService adminService;

    @PutMapping()
    ResponseEntity<ResponseUserDTO> updateAccount(
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = adminService.updateAccount((AppUser) principal, updateUserDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseUserDTO);
    }

    @DeleteMapping()
    ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal Object principal
    ) {
        adminService.deleteAccount((AppUser) principal);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
