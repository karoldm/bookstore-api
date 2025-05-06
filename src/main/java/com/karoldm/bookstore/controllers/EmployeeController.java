package com.karoldm.bookstore.controllers;


import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.services.EmployeeService;
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
@RequestMapping("/v1/store/{storeId}/employee")
@AllArgsConstructor
public class EmployeeController {
    private EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseUserDTO> createEmployee(
            @PathVariable UUID storeId,
            @RequestBody @Valid RegisterUserDTO registerUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = employeeService.createEmployee(storeId, registerUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUserDTO);
    }

    @GetMapping
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<Set<ResponseUserDTO>> listEmployees(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal Object principal
    ) {
        Set<ResponseUserDTO> listEmployees = employeeService.listEmployees(storeId);
        return ResponseEntity.status(HttpStatus.OK).body(listEmployees);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseUserDTO> updateEmployee(
            @PathVariable UUID storeId,
            @PathVariable UUID employeeId,
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = employeeService.updateEmployee(storeId, employeeId, updateUserDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseUserDTO);
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<Void> deleteEmployee(
            @PathVariable UUID storeId,
            @PathVariable UUID employeeId,
            @AuthenticationPrincipal Object principal
    ) {
        employeeService.deleteEmployee(storeId, employeeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
