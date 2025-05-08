package com.karoldm.bookstore.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
public class RequestBookDTO {
    @NotBlank
    private String title;
    @NotNull
    private String summary;
    @NotNull
    private LocalDate releasedAt;
    @Min(1)
    @Max(5)
    private int rating;
    @NotNull
    private boolean available;
    @NotEmpty
    private String author;
    private MultipartFile cover;
}
