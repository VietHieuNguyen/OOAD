package com.example.bookstore.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String errorCode = resolveErrorCode(exception);
        log.warn("Google OAuth2 login failed: code={}, type={}, message={}",
                errorCode,
                exception.getClass().getSimpleName(),
                resolveMessage(exception));
        response.sendRedirect("/login?oauth2Error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8));
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String oauth2Code = oauth2Exception.getError().getErrorCode();
            if ("invalid_google_account".equalsIgnoreCase(oauth2Code)) {
                return "invalid_google_account";
            }
            if ("google_account_sync_failed".equalsIgnoreCase(oauth2Code)) {
                return "google_account_sync_failed";
            }
            if ("access_denied".equalsIgnoreCase(oauth2Code)) {
                return "google_access_denied";
            }
        }

        String message = resolveMessage(exception);
        if (message.contains("authorization request not found")
                || message.contains("authorization_request_not_found")
                || message.contains("state")
                || message.contains("invalid oauth2 authorization request cookie")) {
            return "google_session_expired";
        }
        if (message.contains("invalid_grant")) {
            return "google_invalid_grant";
        }
        if (message.contains("invalid_client")) {
            return "google_invalid_client";
        }
        if (message.contains("account is disabled")) {
            return "account_disabled";
        }
        if (message.contains("connection refused")
                || message.contains("connect timed out")
                || message.contains("i/o error")
                || message.contains("unknownhostexception")) {
            return "google_connection_failed";
        }

        return "oauth2_login_failed";
    }

    private String resolveMessage(Throwable throwable) {
        Throwable current = throwable;
        String lastMessage = "";

        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                lastMessage = current.getMessage();
            }
            current = current.getCause();
        }

        return lastMessage.toLowerCase(Locale.ROOT);
    }
}
