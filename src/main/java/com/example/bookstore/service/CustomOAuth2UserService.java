package com.example.bookstore.service;

import com.example.bookstore.entity.Customer;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.UserRepository;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Tạo Customer mới từ Google
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setUsername(email); // sử dụng email làm username
            customer.setFullName(name != null ? name : "Google User");
            customer.setAuthProvider(AuthProvider.GOOGLE);
            customer.setPasswordHash("GOOGLE_OAUTH2_NO_PASSWORD");
            user = userRepository.save(customer);
        }

        return new DefaultOAuth2User(
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ),
                attributes,
                "email"
        );
    }
}
