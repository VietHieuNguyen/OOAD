package com.example.bookstore.service;

import com.example.bookstore.entity.Cart;
import com.example.bookstore.entity.Customer;
import com.example.bookstore.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý giỏ hàng (phiên bản tối thiểu).
 *
 * <p>Cung cấp các chức năng cơ bản để module Checkout hoạt động độc lập.
 * Thành viên phụ trách Decorator Pattern sẽ bổ sung thêm
 * {@code addItem()}, {@code removeItem()}, {@code updateQuantity()} sau.</p>
 */
@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    /**
     * Lấy giỏ hàng hiện tại của khách hàng, hoặc tạo mới nếu chưa có.
     *
     * @param customer Khách hàng cần lấy giỏ hàng
     * @return Giỏ hàng của khách hàng
     */
    @Transactional
    public Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomer_Id(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Xóa toàn bộ sản phẩm trong giỏ hàng sau khi đặt hàng thành công.
     *
     * @param cart Giỏ hàng cần xóa sạch
     */
    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
