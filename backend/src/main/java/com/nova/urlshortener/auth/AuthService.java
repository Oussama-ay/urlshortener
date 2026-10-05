package com.nova.urlshortener.auth;

import com.nova.urlshortener.user.EmailAlreadyExistsException;
import com.nova.urlshortener.user.User;
import com.nova.urlshortener.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(email, passwordHash);
        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getCreatedAt()
        );
    }
}
