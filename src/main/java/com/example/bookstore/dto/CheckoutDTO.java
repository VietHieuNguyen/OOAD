package com.example.bookstore.dto;

import com.example.bookstore.entity.enums.PaymentMethodType;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhận dữ liệu từ form Checkout.
 */
@Getter
@Setter
public class CheckoutDTO {

    /** Địa chỉ giao hàng do khách hàng nhập. */
    private String address;

    /** Phương thức thanh toán do khách hàng chọn (COD hoặc BANK_TRANSFER). */
    private PaymentMethodType paymentMethod;
}
