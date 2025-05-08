package com.karoldm.bookstore.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UpdateStoreDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slogan;
    private MultipartFile banner;
}
