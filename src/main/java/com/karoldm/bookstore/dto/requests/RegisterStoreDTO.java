package com.karoldm.bookstore.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterStoreDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slogan;
    private String banner;
    @Valid
    private RegisterUserDTO admin;
}
