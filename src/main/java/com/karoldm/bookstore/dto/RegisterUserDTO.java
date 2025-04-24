package com.karoldm.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import javax.management.relation.Role;

@Data
@Builder
public class RegisterUserDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String photo;
}
