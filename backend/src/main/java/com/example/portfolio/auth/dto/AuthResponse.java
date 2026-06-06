package com.example.portfolio.auth.dto;

import com.example.portfolio.auth.domain.UserRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class AuthResponse {
    private String token;
    private Long id;
    private String email;
    private UserRole role;
}
