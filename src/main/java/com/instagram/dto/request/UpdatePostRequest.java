package com.instagram.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "content is required")
    private String content;
}
