-- Chạy đoạn mã này trong MySQL Workbench hoặc Command Line
-- Mật khẩu mặc định là: admin123
INSERT INTO users (user_id, username, email, password_hash, is_active, role, auth_provider)
VALUES (
    UUID(), 
    'admin', 
    'admin@bookstore.com', 
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 
    1, 
    'ADMIN', 
    'LOCAL'
);
