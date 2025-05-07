package com.karoldm.bookstore.dto.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseStoreDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String slogan;
    private String banner;
    @NotBlank
    private Long id;
}
