package com.example.bookstore.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:demo}") String cloudName,
            @Value("${cloudinary.api-key:demo}") String apiKey,
            @Value("${cloudinary.api-secret:demo}") String apiSecret) {
        
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Upload ảnh lên Cloudinary vào folder chỉ định.
     *
     * @param file   File ảnh từ form upload
     * @param folder Thư mục trên Cloudinary (vd: "bookstore/books", "bookstore/categories")
     * @return URL an toàn (HTTPS) của ảnh đã upload
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder
        ));
        return uploadResult.get("secure_url").toString();
    }

    public String uploadAvatar(MultipartFile file) throws IOException {
        return uploadImage(file, "bookstore/avatars");
    }
}

