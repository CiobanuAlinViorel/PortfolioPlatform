package com.example.portfolio.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertContactInfoRequest {
    private String email;
    private String phone;
    private String github;
    private String linkedin;
    private String city;
    private String country;
}
