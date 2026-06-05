package com.campconnect.userservice.service;

import com.campconnect.userservice.dto.*;
import com.campconnect.userservice.entity.*;
import com.campconnect.userservice.entity.User.UserRole;
import com.campconnect.userservice.event.UserEvent;
import com.campconnect.userservice.exception.UserNotFoundException;
import com.campconnect.userservice.feign.*;
import com.campconnect.userservice.messaging.kafka.UserKafkaProducer;
import com.campconnect.userservice.messaging.rabbitmq.UserEventPublisher;
import com.campconnect.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CampSiteClient campSiteClient;

    // ─── Messaging ────────────────────────────────────────────
    private final UserEventPublisher rabbitPublisher;
    private final UserKafkaProducer kafkaProducer;

    // ─── CRUD ────────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)));
    }

    public UserResponse getUserByEmail(String email) {
        return toResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email)));
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé : " + request.getEmail());
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : UserRole.CAMPER)
                .build();

        User saved = userRepository.save(user);
        log.info("Utilisateur créé : id={}", saved.getId());

        // ─── Publier l'événement ──────────────────────────────
        UserEvent event = UserEvent.created(
                saved.getId(), saved.getFirstName(), saved.getLastName(),
                saved.getEmail(), saved.getPhone(), saved.getRole()
        );
        rabbitPublisher.publishUserCreated(event);
        kafkaProducer.sendUserCreated(event);

        return toResponse(saved);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé : " + request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getRole() != null) user.setRole(request.getRole());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("Utilisateur mis à jour : id={}", saved.getId());

        // ─── Publier l'événement ──────────────────────────────
        UserEvent event = UserEvent.updated(
                saved.getId(), saved.getFirstName(), saved.getLastName(),
                saved.getEmail(), saved.getPhone(), saved.getRole()
        );
        rabbitPublisher.publishUserUpdated(event);
        kafkaProducer.sendUserUpdated(event);

        return toResponse(saved);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String email = user.getEmail();
        tokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        log.info("Utilisateur supprimé : id={}", id);

        // ─── Publier l'événement ──────────────────────────────
        UserEvent event = UserEvent.deleted(id, email);
        rabbitPublisher.publishUserDeleted(event);
        kafkaProducer.sendUserDeleted(event);
    }

    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── FORGOT PASSWORD ────────────────────────────────────

    public Map<String, String> forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        tokenRepository.deleteByUserId(user.getId());

        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(code)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        emailService.sendResetPasswordEmail(email, code);

        // ─── Publier l'événement ──────────────────────────────
        UserEvent event = UserEvent.passwordReset(user.getId(), email);
        rabbitPublisher.publishPasswordReset(event);
        kafkaProducer.sendPasswordReset(event);

        return Map.of("message", "Code de réinitialisation envoyé à " + email);
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Code invalide"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Code expiré — veuillez refaire une demande");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Ce code a déjà été utilisé");
        }

        User user = userRepository.findById(resetToken.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(resetToken.getUser().getId()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return Map.of("message", "Mot de passe réinitialisé avec succès");
    }

    // ─── OpenFeign ──────────────────────────────────────────

    public List<CampSiteResponse> getAvailableCampSites() {
        return campSiteClient.getAllCampSites();
    }

    public CampSiteResponse getCampSiteForUser(Long userId, Long siteId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return campSiteClient.getCampSiteById(siteId);
    }

    // ─── Mapper ─────────────────────────────────────────────

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}