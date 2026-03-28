package com.example.bookstore.service;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer registerCustomer(String username, String email, String rawPassword,
                                     String fullName, String phoneNumber, String address) {
        String normalizedUsername = normalizeRequiredField(username, "Username khong duoc de trong.");
        String normalizedEmail = normalizeEmail(email);
        String normalizedFullName = normalizeRequiredField(fullName, "Ho ten khong duoc de trong.");
        String normalizedPassword = rawPassword == null ? "" : rawPassword.trim();

        if (normalizedPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu.");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username da ton tai!");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email da duoc su dung!");
        }

        Customer customer = new Customer();
        customer.setUsername(normalizedUsername);
        customer.setEmail(normalizedEmail);
        customer.setPasswordHash(passwordEncoder.encode(normalizedPassword));
        customer.setFullName(normalizedFullName);
        customer.setPhoneNumber(trimToNull(phoneNumber));
        customer.setAddress(trimToNull(address));
        customer.setAuthProvider(AuthProvider.LOCAL);

        return userRepository.save(customer);
    }

    private String normalizeEmail(String email) {
        String normalizedEmail = normalizeRequiredField(email, "Email khong duoc de trong.");
        return normalizedEmail.toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredField(String value, String errorMessage) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
        return normalizedValue;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
