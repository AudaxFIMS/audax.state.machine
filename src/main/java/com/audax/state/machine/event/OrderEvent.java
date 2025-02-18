package com.audax.state.machine.event;

public enum OrderEvent {
	PROCESS_ORDER,
	SHIPPING,
	SHIPPING_LABEL_CREATED,
	SHIPPING_DONE,
	DELIVERY,
	DELIVERY_LABEL_CREATED,
	DELIVERY_DONE,
	NEXT_STEP, CANCEL_ORDER
}