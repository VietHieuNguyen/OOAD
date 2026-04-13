package com.example.bookstore.service;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.CustomerRepository;
import com.example.bookstore.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================== ADMIN: CUSTOMER MANAGEMENT ========================

    /**
     * Lấy danh sách tất cả Customer để Admin quản lý.
     */
    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    /**
     * Tìm User theo ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Bật/tắt trạng thái active của user.
     */
    @Transactional
    public void toggleActive(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userRepository.save(user);
    }

    // ======================== REGISTRATION ========================

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
