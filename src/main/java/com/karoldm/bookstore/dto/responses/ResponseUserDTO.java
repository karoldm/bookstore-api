package com.karoldm.bookstore.dto.responses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseUserDTO {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotBlank
    private String role;
}
