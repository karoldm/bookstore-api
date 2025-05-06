package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.UpdateStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.services.StoreService;
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

import java.util.UUID;

@RestController
@RequestMapping("/v1/store")
@AllArgsConstructor
public class StoreController {
    private StoreService storeService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a store",
            description = "allow admin or employee get your store information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "listed successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.canAccessStore(principal, #id)")
    ResponseEntity<ResponseStoreDTO> getStoreById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseStoreDTO response = storeService.getStore(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "update a store",
            description = "allow admin update your own store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "updated successfully"),
            @ApiResponse(responseCode = "404", description = "store not found"),
            @ApiResponse(responseCode = "401", description = "user does not have permission role to do it in this store"),
            @ApiResponse(responseCode = "400", description = "invalid body",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("@storeSecurityService.isStoreAdmin(principal, #id)")
    ResponseEntity<ResponseStoreDTO> updateStore(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateStoreDTO updateStoreDTO,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseStoreDTO response = storeService.updateStore(id, updateStoreDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
