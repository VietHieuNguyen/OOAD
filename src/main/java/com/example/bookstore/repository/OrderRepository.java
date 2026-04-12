package com.example.bookstore.repository;

import com.example.bookstore.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, String> {

    /** Customer: lấy danh sách đơn hàng kèm items và payments (để hiển thị size và method). */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.payments WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<Order> findByCustomer_IdWithDetails(@Param("customerId") String customerId);

    /** Admin: lấy tất cả đơn kèm items + customer (tránh LazyInitializationException). */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book LEFT JOIN FETCH o.customer ORDER BY o.orderDate DESC")
    List<Order> findAllWithItemsOrderByOrderDateDesc();

    /** Lấy chi tiết 1 đơn kèm items + customer + payments + shippings. */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.book LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.payments LEFT JOIN FETCH o.shippings WHERE o.orderId = :id")
    Optional<Order> findByIdWithItems(@Param("id") String id);
}
