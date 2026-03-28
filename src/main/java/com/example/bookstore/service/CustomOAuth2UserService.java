package com.example.bookstore.service;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.UserRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String GOOGLE_PASSWORD_PLACEHOLDER = "GOOGLE_OAUTH2_NO_PASSWORD";
    private static final int MAX_USERNAME_LENGTH = 100;
    private static final int MAX_FULL_NAME_LENGTH = 150;

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        try {
            String email = extractRequiredEmail(attributes);
            String displayName = extractDisplayName(attributes, email);

            User user = userRepository.findByEmail(email)
                    .map(existingUser -> prepareExistingUser(existingUser, displayName))
                    .orElseGet(() -> createGoogleCustomer(email, displayName));

            Map<String, Object> principalAttributes = new HashMap<>(attributes);
            principalAttributes.put(USERNAME_ATTRIBUTE, user.getUsername());
            principalAttributes.put("userId", user.getId());
            principalAttributes.put("email", user.getEmail());
            principalAttributes.put("role", user.getRole().name());

            return new DefaultOAuth2User(
                    Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    ),
                    principalAttributes,
                    USERNAME_ATTRIBUTE
            );
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Failed to sync Google account into local database", ex);
            throw oauth2AuthenticationException("google_account_sync_failed", "Không đồng bộ được tài khoản Google");
        }
    }

    private User prepareExistingUser(User user, String displayName) {
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw oauth2AuthenticationException("invalid_google_account", "Account is disabled");
        }

        if (user instanceof Customer customer
                && user.getAuthProvider() == AuthProvider.GOOGLE
                && isBlank(customer.getFullName())) {
            customer.setFullName(truncate(displayName, MAX_FULL_NAME_LENGTH));
        }

        return user;
    }

    private User createGoogleCustomer(String email, String displayName) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setUsername(generateUniqueUsername(email));
        customer.setFullName(truncate(displayName, MAX_FULL_NAME_LENGTH));
        customer.setAuthProvider(AuthProvider.GOOGLE);
        customer.setPasswordHash(GOOGLE_PASSWORD_PLACEHOLDER);
        return userRepository.save(customer);
    }

    private String extractRequiredEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        if (email instanceof String emailValue && !emailValue.isBlank()) {
            return emailValue.trim().toLowerCase(Locale.ROOT);
        }
        throw oauth2AuthenticationException("invalid_google_account", "Google account does not provide an email");
    }

    private String extractDisplayName(Map<String, Object> attributes, String email) {
        Object name = attributes.get("name");
        if (name instanceof String nameValue && !nameValue.isBlank()) {
            return nameValue.trim();
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }

        return "Google User";
    }

    private String generateUniqueUsername(String email) {
        int atIndex = email.indexOf('@');
        String base = atIndex > 0 ? email.substring(0, atIndex) : email;
        String normalizedBase = normalizeUsername(base);

        String candidate = normalizedBase;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String suffixText = String.valueOf(suffix++);
            candidate = truncateUsername(normalizedBase, suffixText.length()) + suffixText;
        }

        return candidate;
    }

    private String normalizeUsername(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9._-]", "");
        if (normalized.isBlank()) {
            return "googleuser";
        }
        return truncateUsername(normalized, 0);
    }

    private String truncateUsername(String value, int suffixLength) {
        int maxLength = Math.max(1, MAX_USERNAME_LENGTH - suffixLength);
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private OAuth2AuthenticationException oauth2AuthenticationException(String errorCode, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(errorCode), message);
    }
}
