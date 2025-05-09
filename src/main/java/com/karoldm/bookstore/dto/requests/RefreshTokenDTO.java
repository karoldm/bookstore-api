package com.karoldm.bookstore.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenDTO {
    @NotBlank
    private String refreshToken;
}
