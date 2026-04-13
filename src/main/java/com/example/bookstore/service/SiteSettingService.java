package com.example.bookstore.service;

import com.example.bookstore.entity.SiteSetting;
import com.example.bookstore.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý cài đặt chung của website (Site Settings).
 * <p>Lưu trữ dạng key-value: site_name, hero_image_url, v.v.</p>
 */
@Service
public class SiteSettingService {

    private static final String KEY_SITE_NAME = "site_name";
    private static final String KEY_HERO_IMAGE = "hero_image_url";
    private static final String KEY_SUPPORT_EMAIL = "support_email";

    private static final String DEFAULT_SITE_NAME = "X-Books";

    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    /**
     * Lấy tên website hiện tại.
     */
    @Transactional(readOnly = true)
    public String getSiteName() {
        return siteSettingRepository.findById(KEY_SITE_NAME)
                .map(SiteSetting::getSettingValue)
                .orElse(DEFAULT_SITE_NAME);
    }

    /**
     * Lấy URL ảnh hero banner trang chủ.
     */
    @Transactional(readOnly = true)
    public String getHeroImageUrl() {
        return siteSettingRepository.findById(KEY_HERO_IMAGE)
                .map(SiteSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * Lấy email liên hệ / hỗ trợ.
     */
    @Transactional(readOnly = true)
    public String getSupportEmail() {
        return siteSettingRepository.findById(KEY_SUPPORT_EMAIL)
                .map(SiteSetting::getSettingValue)
                .orElse("support@xbooks.com");
    }

    /**
     * Lưu tên website.
     */
    @Transactional
    public void saveSiteName(String siteName) {
        saveSetting(KEY_SITE_NAME, siteName != null ? siteName.trim() : DEFAULT_SITE_NAME);
    }

    /**
     * Lưu URL ảnh hero banner.
     */
    @Transactional
    public void saveHeroImageUrl(String url) {
        saveSetting(KEY_HERO_IMAGE, url != null ? url.trim() : null);
    }

    /**
     * Lưu email liên hệ.
     */
    @Transactional
    public void saveSupportEmail(String email) {
        saveSetting(KEY_SUPPORT_EMAIL, email != null ? email.trim() : null);
    }


    /**
     * Lưu một cặp key-value vào bảng site_settings.
     */
    @Transactional
    public void saveSetting(String key, String value) {
        SiteSetting setting = siteSettingRepository.findById(key)
                .orElse(new SiteSetting(key, null));
        setting.setSettingValue(value);
        siteSettingRepository.save(setting);
    }
}
