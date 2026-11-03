package com.example.passwordreset.service;

import com.example.passwordreset.entity.User;
import com.example.passwordreset.entity.PasswordResetToken;
import com.example.passwordreset.repository.UserRepository;
import com.example.passwordreset.repository.PasswordResetTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Demander la réinitialisation du mot de passe

    @Transactional
    public void forgotPassword(String email) {

        Optional<User> optionalUser =
                userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        // Invalider les anciens jetons non utilisés
        var oldTokens =
                tokenRepository.findByUserAndUsedFalse(user);

        for (PasswordResetToken oldToken : oldTokens) {
            oldToken.markAsUsed();
        }

        tokenRepository.saveAll(oldTokens);

        // Générer un nouveau jeton
        String token = generateToken();

        // Calculer son hash
        String tokenHash = hashToken(token);

        // Créer l'objet PasswordResetToken
        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setTokenHash(tokenHash);
        resetToken.setUser(user);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(15)
        );

        resetToken.setUsed(false);

        // Enregistrer le jeton
        tokenRepository.save(resetToken);

        // Construire le lien
        String resetLink =
                frontendUrl + "/reset-password?token=" + token;

        // Envoyer l'e-mail
        emailService.sendResetEmail(email, resetLink);
    }


    // 2. Réinitialiser le mot de passe

    @Transactional
    public void resetPassword(String token, String newPassword) {

        // Calculer le hash du jeton reçu
        String tokenHash = hashToken(token);

        // Rechercher le jeton dans la base
        PasswordResetToken resetToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                            new IllegalArgumentException(
                                "Lien invalide ou expiré"
                            )
                        );

        // Vérifier si le jeton a déjà été utilisé
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException(
                    "Lien invalide ou expiré"
            );
        }

        // Vérifier la date d'expiration
        if (resetToken.isExpired()) {
            throw new IllegalArgumentException(
                    "Lien invalide ou expiré"
            );
        }

        // Récupérer l'utilisateur
        User user = resetToken.getUser();

        // Encoder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));

        // Enregistrer le nouveau mot de passe
        userRepository.save(user);

        // Invalider le jeton
        resetToken.markAsUsed();

        tokenRepository.save(resetToken);
    }


    // 3. Générer un jeton aléatoire

    private String generateToken() {

        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }


    // 4. Calculer le hash SHA-256

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(e);
        }
    }
}