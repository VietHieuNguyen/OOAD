package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.enums.AuthProvider;
import com.example.bookstore.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForgotPasswordService {

    private static final String OTP_PREFIX = "FORGOT_PASSWORD_OTP";
    private static final String VERIFIED_PREFIX = "FORGOT_PASSWORD_VERIFIED";
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public ForgotPasswordService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public String sendOtp(String rawEmail) {
        User user = getEligibleUser(rawEmail);
        String otp = generateOtp();
        long expiresAt = Instant.now().plus(OTP_TTL).toEpochMilli();

        user.setToken(buildOtpToken(otp, expiresAt));
        userRepository.save(user);

        try {
            sendOtpEmail(user.getEmail(), otp);
        } catch (MailException ex) {
            user.setToken(null);
            userRepository.save(user);
            throw new IllegalStateException("Không gửi được OTP. Vui lòng kiểm tra cấu hình email");
        }

        return user.getEmail();
    }

    @Transactional
    public String verifyOtp(String rawEmail, String rawOtp) {
        User user = getEligibleUser(rawEmail);
        TokenState tokenState = parseToken(user.getToken());
        String otp = rawOtp == null ? "" : rawOtp.trim();

        if (!tokenState.isOtpState()) {
            throw new IllegalStateException("OTP không hợp lệ hoặc đã hết hạn.");
        }
        if (tokenState.isExpired()) {
            user.setToken(null);
            userRepository.save(user);
            throw new IllegalStateException("OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (!tokenState.value().equals(otp)) {
            throw new IllegalArgumentException("OTP không đúng.");
        }

        long verifiedExpiresAt = Instant.now().plus(VERIFIED_TTL).toEpochMilli();
        user.setToken(buildVerifiedToken(verifiedExpiresAt));
        userRepository.save(user);
        return user.getEmail();
    }

    @Transactional(readOnly = true)
    public String validateResetAccess(String rawEmail) {
        User user = getEligibleUser(rawEmail);
        TokenState tokenState = parseToken(user.getToken());

        if (!tokenState.isVerifiedState() || tokenState.isExpired()) {
            throw new IllegalStateException("Bạn cần xác thực OTP trước khi đổi mật khẩu.");
        }

        return user.getEmail();
    }

    @Transactional
    public void resetPassword(String rawEmail, String newPassword) {
        User user = getEligibleUser(rawEmail);
        TokenState tokenState = parseToken(user.getToken());

        if (!tokenState.isVerifiedState() || tokenState.isExpired()) {
            throw new IllegalStateException("Phiên đổi mật khẩu đã hết hạn. Vui lòng bắt đầu lại.");
        }

        String normalizedPassword = newPassword == null ? "" : newPassword.trim();
        if (normalizedPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        user.setPasswordHash(passwordEncoder.encode(normalizedPassword));
        user.setToken(null);
        userRepository.save(user);
    }

    private User getEligibleUser(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại."));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalStateException("Tài khoản đã bị khóa.");
        }
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new IllegalStateException("Tài khoản này đang dùng Google Sign-In, không đặt lại mật khẩu cục bộ.");
        }

        return user;
    }

    private void sendOtpEmail(String recipient, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(recipient);
        message.setSubject("BookStore OTP reset password");
        message.setText("Mã OTP đặt lại mật khẩu của bạn là: " + otp + "\n\n"
                + "Mã có hiệu lực trong 10 phút.\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.");
        mailSender.send(message);
    }

    private String normalizeEmail(String rawEmail) {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        return email;
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int value = secureRandom.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    private String buildOtpToken(String otp, long expiresAt) {
        return OTP_PREFIX + ":" + otp + ":" + expiresAt;
    }

    private String buildVerifiedToken(long expiresAt) {
        return VERIFIED_PREFIX + ":" + expiresAt;
    }

    private TokenState parseToken(String token) {
        if (token == null || token.isBlank()) {
            return TokenState.invalid();
        }

        String[] parts = token.split(":");
        try {
            if (parts.length == 3 && OTP_PREFIX.equals(parts[0])) {
                return TokenState.otp(parts[1], Long.parseLong(parts[2]));
            }
            if (parts.length == 2 && VERIFIED_PREFIX.equals(parts[0])) {
                return TokenState.verified(Long.parseLong(parts[1]));
            }
        } catch (NumberFormatException ignored) {
            return TokenState.invalid();
        }

        return TokenState.invalid();
    }

    private record TokenState(TokenType type, String value, long expiresAt) {

        private static TokenState otp(String otp, long expiresAt) {
            return new TokenState(TokenType.OTP, otp, expiresAt);
        }

        private static TokenState verified(long expiresAt) {
            return new TokenState(TokenType.VERIFIED, "", expiresAt);
        }

        private static TokenState invalid() {
            return new TokenState(TokenType.INVALID, "", 0L);
        }

        private boolean isOtpState() {
            return type == TokenType.OTP;
        }

        private boolean isVerifiedState() {
            return type == TokenType.VERIFIED;
        }

        private boolean isExpired() {
            return Instant.now().toEpochMilli() > expiresAt;
        }
    }

    private enum TokenType {
        OTP,
        VERIFIED,
        INVALID
    }
}
