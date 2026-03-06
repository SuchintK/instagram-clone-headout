package com.instagram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {
    private String id;
    private String postId;
    private String s3URL;
    private Long createdAt;
    private Long updatedAt;
}
