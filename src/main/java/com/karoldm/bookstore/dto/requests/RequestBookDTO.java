package com.karoldm.bookstore.dto.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RequestBookDTO {
    @NotBlank
    private String title;
    @NotNull
    private String summary;
    @NotNull
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate releasedAt;
    @Min(1)
    @Max(5)
    private int rating;
    @NotNull
    private boolean available;
    @NotEmpty
    private String author;
    private String cover;
}
