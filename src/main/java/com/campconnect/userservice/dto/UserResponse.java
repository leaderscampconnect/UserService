package com.campconnect.userservice.dto;

import com.campconnect.userservice.entity.User.UserRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime createdAt;
}