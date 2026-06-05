package com.campconnect.userservice.event;

import com.campconnect.userservice.entity.User.UserRole;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent implements Serializable {

    private String eventType;       // USER_CREATED, USER_UPDATED, USER_DELETED, PASSWORD_RESET
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private LocalDateTime eventTime;

    // Factory methods pour chaque type d'événement
    public static UserEvent created(Long id, String firstName, String lastName,
                                    String email, String phone, UserRole role) {
        return UserEvent.builder()
                .eventType("USER_CREATED")
                .userId(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .role(role)
                .eventTime(LocalDateTime.now())
                .build();
    }

    public static UserEvent updated(Long id, String firstName, String lastName,
                                    String email, String phone, UserRole role) {
        return UserEvent.builder()
                .eventType("USER_UPDATED")
                .userId(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .role(role)
                .eventTime(LocalDateTime.now())
                .build();
    }

    public static UserEvent deleted(Long id, String email) {
        return UserEvent.builder()
                .eventType("USER_DELETED")
                .userId(id)
                .email(email)
                .eventTime(LocalDateTime.now())
                .build();
    }

    public static UserEvent passwordReset(Long id, String email) {
        return UserEvent.builder()
                .eventType("PASSWORD_RESET")
                .userId(id)
                .email(email)
                .eventTime(LocalDateTime.now())
                .build();
    }
}