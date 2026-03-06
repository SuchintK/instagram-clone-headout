package com.instagram.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    private String id;
    private String postId;
    private String s3URL;
    private Long createdAt;
    private Long updatedAt;
    private boolean inactiveFlag;
}
