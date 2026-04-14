package com.example.bookstore.repository;

import com.example.bookstore.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AddressRepository extends JpaRepository<Address, String> {

    /** Tất cả địa chỉ của customer, default trước. */
    List<Address> findByCustomerIdOrderByIsDefaultDescIdAsc(String customerId);

    /** Địa chỉ default của customer. */
    Optional<Address> findByCustomerIdAndIsDefaultTrue(String customerId);

    /** Unset tất cả default của customer (để set mới). */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void clearDefaultForCustomer(@Param("customerId") String customerId);

    long countByCustomerId(String customerId);
}
