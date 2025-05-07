package com.karoldm.bookstore.controllers;


import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.services.EmployeeService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/v1/store/{storeId}/employee")
@AllArgsConstructor
public class EmployeeController {
    private EmployeeService employeeService;

    @PostMapping
    @Operation(
            summary = "create new employee",
            description = "allow admin create new employee on your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "created successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseUserDTO> createEmployee(
            @PathVariable Long storeId,
            @RequestBody @Valid RegisterUserDTO registerUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = employeeService.createEmployee(storeId, registerUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUserDTO);
    }

    @GetMapping
    @Operation(
            summary = "list all employees",
            description = "allow admin list all employees of your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "listed successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<Set<ResponseUserDTO>> listEmployees(
            @PathVariable Long storeId,
            @AuthenticationPrincipal Object principal
    ) {
        Set<ResponseUserDTO> listEmployees = employeeService.listEmployees(storeId);
        return ResponseEntity.status(HttpStatus.OK).body(listEmployees);
    }

    @PutMapping("/{employeeId}")
    @Operation(
            summary = "updated an employee",
            description = "allow admin updated an employee of your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated successfully"),
            @ApiResponse(responseCode = "404", description = "store or employee not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<ResponseUserDTO> updateEmployee(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestBody @Valid UpdateUserDTO updateUserDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseUserDTO responseUserDTO = employeeService.updateEmployee(storeId, employeeId, updateUserDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseUserDTO);
    }

    @DeleteMapping("/{employeeId}")
    @Operation(
            summary = "delete an employee",
            description = "allow admin delete an employee of your store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "deleted successfully"),
            @ApiResponse(responseCode = "404", description = "store or employee not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #storeId)")
    ResponseEntity<Void> deleteEmployee(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal Object principal
    ) {
        employeeService.deleteEmployee(storeId, employeeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
