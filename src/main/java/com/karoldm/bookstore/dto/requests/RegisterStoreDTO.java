package com.karoldm.bookstore.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class RegisterStoreDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slogan;
    private MultipartFile banner;
    @NotBlank
    private String adminName;
    @NotBlank
    private String username;
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
