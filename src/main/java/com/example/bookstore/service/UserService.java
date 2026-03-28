package com.example.bookstore.service;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer registerCustomer(String username, String email, String rawPassword,
                                     String fullName, String phoneNumber, String address) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username đã tồn tại!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setPasswordHash(passwordEncoder.encode(rawPassword));
        customer.setFullName(fullName);
        customer.setPhoneNumber(phoneNumber);
        customer.setAddress(address);
        customer.setAuthProvider(AuthProvider.LOCAL);

        return userRepository.save(customer);
    }
}
