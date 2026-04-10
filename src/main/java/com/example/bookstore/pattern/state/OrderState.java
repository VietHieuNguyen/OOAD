package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

/**
 * State Pattern - State Interface
 */
public interface OrderState {
    void setContext(Order context);
    void nextState();
    void cancelOrder();
    OrderStatus getStatusName();
}
