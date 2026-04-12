package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;

public class PendingState implements OrderState {
    private Order context;

    @Override
    public void setContext(Order context) {
        this.context = context;
    }

    @Override
    public void nextState() {
        // Chuyển từ PENDING sang CONFIRMED
        OrderState newState = new ConfirmedState();
        this.context.changeState(newState);
    }

    @Override
    public void cancelOrder() {
        // Có thể hủy khi đang chờ xác nhận
        OrderState newState = new CancelledState();
        this.context.changeState(newState);
    }

    @Override
    public OrderStatus getStatusName() {
        return OrderStatus.PENDING;
    }
}
