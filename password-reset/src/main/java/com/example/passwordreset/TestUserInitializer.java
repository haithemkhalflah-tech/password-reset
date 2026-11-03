package com.example.passwordreset;

import com.example.passwordreset.entity.User;
import com.example.passwordreset.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TestUserInitializer {

    @Bean
    CommandLineRunner createTestUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String email = "haytham.khalflah@etudiant-fst.utm.tn";

            if (userRepository.findByEmail(email).isEmpty()) {
                User user = new User();
                user.setEmail(email);
                user.setPassword(
                    passwordEncoder.encode("OldPassword123!")
                );

                userRepository.save(user);

                System.out.println("Test user created: " + email);
            }
        };
    }
}