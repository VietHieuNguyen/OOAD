package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

public class CompletedState implements OrderState {
    private Order context;

    @Override
    public void setContext(Order context) {
        this.context = context;
    }

    @Override
    public void nextState() {
        throw new IllegalStateException("Đơn hàng đã hoàn thành, không thể chuyển trạng thái tiếp theo.");
    }

    @Override
    public void cancelOrder() {
        throw new IllegalStateException("Đơn hàng đã hoàn thành, không thể hủy.");
    }

    @Override
    public OrderStatus getStatusName() {
        return OrderStatus.COMPLETED;
    }
}
