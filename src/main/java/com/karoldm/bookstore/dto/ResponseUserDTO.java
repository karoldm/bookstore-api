package com.karoldm.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import javax.management.relation.Role;
import java.util.UUID;

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
