package com.example.bookstore.pattern.state;

import com.example.bookstore.entity.enums.OrderStatus;

public class StateFactory {
    public static OrderState getState(OrderStatus status) {
        if (status == null) {
            return new PendingState();
        }
        switch (status) {
            case PENDING:
                return new PendingState();
            case CONFIRMED:
                return new ConfirmedState();
            case COMPLETED:
                return new CompletedState();
            case CANCELLED:
                return new CancelledState();
            default:
                return new PendingState();
        }
    }
}
