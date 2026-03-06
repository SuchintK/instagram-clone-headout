package com.instagram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private String id;
    private String content;
    private String userId;
    private Long createdAt;
    private Long updatedAt;
    private List<MediaResponse> media;
}
