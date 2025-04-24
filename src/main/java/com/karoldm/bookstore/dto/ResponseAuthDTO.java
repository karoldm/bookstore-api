package com.karoldm.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseAuthDTO {
    @NotBlank
    private String token;
    @NotBlank
    private String refreshToken;
    @NotNull
    private ResponseUserDTO user;
    @NotNull
    private ResponseStoreDTO store;
}
