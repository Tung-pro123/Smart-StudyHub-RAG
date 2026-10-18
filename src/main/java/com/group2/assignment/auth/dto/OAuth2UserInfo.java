package com.group2.assignment.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String googleId;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String provider; // "google"
}
