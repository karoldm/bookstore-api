package com.karoldm.bookstore.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
