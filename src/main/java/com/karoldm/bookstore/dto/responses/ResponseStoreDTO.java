package com.karoldm.bookstore.dto.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResponseStoreDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slogan;
    private String banner;
    @NotBlank
    private UUID id;
}
