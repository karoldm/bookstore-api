package com.karoldm.bookstore.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ResponseBookDTO {
    private UUID id;
    private String title;
    private String summary;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate releasedAt;
    private int rating;
    private boolean available;
    private String author;
    private String cover;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate createdAt;
}
