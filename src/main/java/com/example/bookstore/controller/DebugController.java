package com.example.bookstore.controller;

import com.example.bookstore.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;

@RestController
public class DebugController {

    private final UserRepository userRepository;

    public DebugController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/debug/users")
    public String getUsers() {
        return userRepository.findAll().stream()
                .map(u -> "User: " + u.getUsername() + " | Hash: " + u.getPasswordHash() + " | Active: " + u.getIsActive())
                .collect(Collectors.joining("<br>"));
    }

    @GetMapping("/debug/testpass")
    public String testPass(@org.springframework.web.bind.annotation.RequestParam("username") String username, 
                           @org.springframework.web.bind.annotation.RequestParam("raw") String rawPass) {
        com.example.bookstore.entity.User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return "User not found";
        
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        boolean match = enc.matches(rawPass, user.getPasswordHash());
        return "User: " + username + " | Hash: " + user.getPasswordHash() + " | Raw: " + rawPass + " | Match: " + match;
    }
}
