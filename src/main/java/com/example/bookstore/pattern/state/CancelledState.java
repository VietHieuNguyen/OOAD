package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

public class CancelledState implements OrderState {
    private Order context;

    @Override
    public void setContext(Order context) {
        this.context = context;
    }

    @Override
    public void nextState() {
        throw new IllegalStateException("Đơn hàng đã hủy, không thể tiếp tục vòng đời.");
    }

    @Override
    public void cancelOrder() {
        throw new IllegalStateException("Đơn hàng đã được hủy trước đó, không thể hủy thêm lần nữa.");
    }

    @Override
    public OrderStatus getStatusName() {
        return OrderStatus.CANCELLED;
    }
}
