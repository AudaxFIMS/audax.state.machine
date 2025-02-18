package com.audax.state.machine.guard;

import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.state.OrderState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

public class ShippingDoneGuard implements Guard<OrderState, OrderEvent> {
	@Override
	public boolean evaluate(StateContext<OrderState, OrderEvent> context) {
		return context.getStateMachine().getState().getIds().contains(OrderState.SHIPPING_STAGE);
	}
}
