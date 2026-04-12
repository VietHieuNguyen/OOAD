package com.example.bookstore.repository;

import com.example.bookstore.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, String> {

    /** Tất cả địa chỉ của customer, default trước. */
    List<CustomerAddress> findByCustomerIdOrderByIsDefaultDescIdAsc(String customerId);

    /** Địa chỉ default của customer. */
    Optional<CustomerAddress> findByCustomerIdAndIsDefaultTrue(String customerId);

    /** Unset tất cả default của customer (để set mới). */
    @Modifying
    @Transactional
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void clearDefaultForCustomer(@Param("customerId") String customerId);

    long countByCustomerId(String customerId);
}
