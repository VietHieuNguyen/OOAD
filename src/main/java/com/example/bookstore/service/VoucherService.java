package com.example.bookstore.service;

import com.example.bookstore.entity.Voucher;
import com.example.bookstore.repository.VoucherRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý Voucher (mã giảm giá).
 */
@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Voucher> findById(String id) {
        return voucherRepository.findById(id);
    }

    @Transactional
    public Voucher save(Voucher voucher) {
        // Chuẩn hóa mã voucher thành uppercase
        if (voucher.getCode() != null) {
            voucher.setCode(voucher.getCode().trim().toUpperCase());
        }
        return voucherRepository.save(voucher);
    }

    @Transactional
    public void deleteById(String id) {
        if (!voucherRepository.existsById(id)) {
            throw new IllegalArgumentException("Voucher not found with ID: " + id);
        }
        voucherRepository.deleteById(id);
    }

    /**
     * Bật/tắt trạng thái active của voucher.
     */
    @Transactional
    public void toggleActive(String id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));
        voucher.setIsActive(!Boolean.TRUE.equals(voucher.getIsActive()));
        voucherRepository.save(voucher);
    }
}
