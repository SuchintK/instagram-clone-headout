package com.instagram.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "content is required")
    private String content;

    private List<String> mediaUrls;
}
