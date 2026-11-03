package com.example.passwordreset.controller;

import com.example.passwordreset.dto.ForgotPasswordRequest;
import com.example.passwordreset.dto.ResetPasswordRequest;
import com.example.passwordreset.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final PasswordResetService passwordResetService;

    public AuthController(
            PasswordResetService passwordResetService) {

        this.passwordResetService = passwordResetService;
    }

    // Demande de réinitialisation

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.forgotPassword(request.email());

        return ResponseEntity.ok(
            Map.of(
                "message",
                "Si cette adresse correspond à un compte, vous recevrez un e-mail contenant les instructions."
            )
        );
    }


    // Réinitialisation du mot de passe

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        try {

            passwordResetService.resetPassword(
                    request.token(),
                    request.newPassword()
            );

            return ResponseEntity.ok(
                Map.of("message", "Mot de passe réinitialisé avec succès")
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("message", "Lien invalide ou expiré")
            );
        }
    }
}