package com.karoldm.bookstore.dto.requests;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserDTO {
    private String name;
    private String password;
}
