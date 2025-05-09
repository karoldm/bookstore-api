package com.karoldm.bookstore.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseRefreshTokenDTO {
    private String token;
    private String refreshToken;
}
