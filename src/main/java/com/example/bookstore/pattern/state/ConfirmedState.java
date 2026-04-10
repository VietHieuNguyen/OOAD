package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

public class ConfirmedState implements OrderState {
    private Order context;

    @Override
    public void setContext(Order context) {
        this.context = context;
    }

    @Override
    public void nextState() {
        // Chuyển từ CONFIRMED sang COMPLETED (Giao xong)
        OrderState newState = new CompletedState();
        this.context.changeState(newState);
    }

    @Override
    public void cancelOrder() {
        // Có thể hủy khi đã xác nhận nhưng chưa giao xong
        OrderState newState = new CancelledState();
        this.context.changeState(newState);
    }

    @Override
    public OrderStatus getStatusName() {
        return OrderStatus.CONFIRMED;
    }
}
