package com.example.passwordreset.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetEmail(String email, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(email);
        message.setSubject("Réinitialisation de votre mot de passe");

        message.setText(
            "Bonjour,\n\n" +
            "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
            "Cliquez sur le lien suivant pour choisir un nouveau mot de passe :\n\n" +
            resetLink + "\n\n" +
            "Ce lien expire dans 15 minutes et ne peut être utilisé qu'une seule fois.\n\n" +
            "Si vous n'avez pas effectué cette demande, ignorez cet e-mail."
        );

        mailSender.send(message);
    }
}