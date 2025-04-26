package com.karoldm.bookstore.dto.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseUserDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotBlank
    private String role;
    private String photo;
}
