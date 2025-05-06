package com.karoldm.bookstore.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateBookAvailableDTO {
    @NotNull
    private Boolean available;
}
