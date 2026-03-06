package com.instagram.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String name;
    private String phoneNumber;
    private Set<String> followers;
    private Set<String> following;
    private boolean inactiveFlag;

    public User(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.followers = new HashSet<>();
        this.following = new HashSet<>();
        this.inactiveFlag = false;
    }
}
