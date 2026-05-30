package com.campconnect.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("CampConnect — Réinitialisation de mot de passe");
            helper.setText(buildEmailBody(resetToken), true);

            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à : {}", toEmail);

        } catch (Exception e) {
            log.error("Erreur envoi email à {} : {}", toEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email");
        }
    }

    private String buildEmailBody(String token) {
        return """
            <div style="font-family: sans-serif; max-width: 500px; margin: auto; padding: 30px;
                        border: 1px solid #e2e8e4; border-radius: 12px;">
              <h2 style="color: #1a6b3c;">🏕 CampConnect</h2>
              <p>Vous avez demandé une réinitialisation de mot de passe.</p>
              <p>Votre code de réinitialisation est :</p>
              <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                          color: #1a6b3c; text-align: center; padding: 20px;
                          background: #e8f4f0; border-radius: 8px; margin: 20px 0;">
                %s
              </div>
              <p style="color: #6b7280; font-size: 13px;">
                Ce code expire dans <strong>15 minutes</strong>.<br>
                Si vous n'avez pas fait cette demande, ignorez cet email.
              </p>
            </div>
            """.formatted(token);
    }
}