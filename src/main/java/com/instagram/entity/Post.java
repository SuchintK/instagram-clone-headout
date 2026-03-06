package com.instagram.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private String id;
    private String content;
    private Long createdAt;
    private Long updatedAt;
    private String userId;
    private boolean inactiveFlag;
}
