package com.karoldm.bookstore.dto.requests;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BooksFilterDTO {
    private String author;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer rating;
    private Boolean available;
}
