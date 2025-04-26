package com.karoldm.bookstore.controllers;

import com.karoldm.bookstore.dto.requests.UpdateStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.services.StoreSecurityService;
import com.karoldm.bookstore.services.StoreService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @PreAuthorize("@storeSecurityService.canAccessStore(principal, #id)")
    ResponseEntity<ResponseStoreDTO> getStoreById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Object principal
    ) {
        ResponseStoreDTO response = storeService.getStore(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
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
