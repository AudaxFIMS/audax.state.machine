package com.audax.state.machine.state;

public enum OrderState {
    NEW,
    PROCESSING,
    
    // SHIPPING (Parent State)
    SHIPPING,
        SHIPPING_WAITING_LABEL,
        SHIPPING_STAGE,
        SHIPPING_DONE,
    
    // DELIVERING (Parent State)
    DELIVERING,
        DELIVERING_WAITING_LABEL,
        DELIVERING_STAGE,
        DELIVERING_DONE,
    
    ORDER_COMPLETED,
    CANCELED
}