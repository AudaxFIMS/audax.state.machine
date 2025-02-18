package com.audax.state.machine.config;

import java.util.EnumSet;

import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.guard.DeliveringDoneGuard;
import com.audax.state.machine.guard.ShippingDoneGuard;
import com.audax.state.machine.state.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {
	
	private final StateMachineRuntimePersister<OrderState, OrderEvent, String> runtimePersister;
	
	public OrderStateMachineConfig(StateMachineRuntimePersister<OrderState, OrderEvent, String> runtimePersister) {
		this.runtimePersister = runtimePersister;
	}
	
	@Override
	public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
		states.withStates()
			.initial(OrderState.NEW)
			.states(
					EnumSet.of(
						OrderState.NEW,
						OrderState.PROCESSING,
						OrderState.SHIPPING,
						OrderState.DELIVERING,
						OrderState.ORDER_COMPLETED,
						OrderState.CANCELED
					)
			)
			.end(OrderState.ORDER_COMPLETED)
			.end(OrderState.CANCELED)
			// Define SHIPPING as a composite state
			.and().withStates()
				.parent(OrderState.SHIPPING)
				.initial(OrderState.SHIPPING_WAITING_LABEL)
				.state(OrderState.SHIPPING_STAGE)
				.end(OrderState.SHIPPING_DONE)
			// Define DELIVERING as a composite state
			.and().withStates()
				.parent(OrderState.DELIVERING)
				.initial(OrderState.DELIVERING_WAITING_LABEL)
				.state(OrderState.DELIVERING_STAGE)
				.end(OrderState.DELIVERING_DONE);
	}
	
	@Override
	public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
		transitions
				// Cancellation handling
				.withExternal()
					.source(OrderState.NEW)
					.target(OrderState.CANCELED)
					.event(OrderEvent.CANCEL_ORDER)
				// Processing handling
				.and().withExternal()
					.source(OrderState.NEW)
					.target(OrderState.PROCESSING)
					.event(OrderEvent.PROCESS_ORDER)
				.and().withExternal()
					.source(OrderState.PROCESSING)
					.target(OrderState.SHIPPING_WAITING_LABEL)
					.event(OrderEvent.SHIPPING)
				//======================================================================================================
				// SHIPPING Process
				.and().withLocal()
					.state(OrderState.SHIPPING)
					.source(OrderState.SHIPPING_WAITING_LABEL)
					.target(OrderState.SHIPPING_STAGE)
					.event(OrderEvent.SHIPPING_LABEL_CREATED)
				.and().withLocal()
					.state(OrderState.SHIPPING)
					.source(OrderState.SHIPPING_STAGE)
					.target(OrderState.SHIPPING_DONE)
					.event(OrderEvent.SHIPPING_DONE)
					.action(parentStateCompleteAction())
				// **Auto transition from SHIPPING_DONE to DELIVERING_WAITING_LABEL**
				.and().withExternal()
					.source(OrderState.SHIPPING)  // Last substate of SHIPPING
					.target(OrderState.DELIVERING_WAITING_LABEL) // First substate of DELIVERING
					.event(OrderEvent.NEXT_STEP)
					.guard(new ShippingDoneGuard())
					.action(
							context -> {
								System.out.println("Auto-transition from SHIPPING_DONE to DELIVERING_WAITING_LABEL");
							}
					)
				.and().withExternal()
					.source(OrderState.SHIPPING_DONE)  // Last substate of SHIPPING
					.target(OrderState.DELIVERING_WAITING_LABEL) // First substate of DELIVERING
					.event(OrderEvent.NEXT_STEP)
				// }}
				//======================================================================================================
				// DELIVERING Process
				.and().withLocal()
					.state(OrderState.DELIVERING)
					.source(OrderState.DELIVERING_WAITING_LABEL)
					.target(OrderState.DELIVERING_STAGE)
					.event(OrderEvent.DELIVERY_LABEL_CREATED)
				.and().withLocal()
					.state(OrderState.DELIVERING)
					.source(OrderState.DELIVERING_STAGE)
					.target(OrderState.DELIVERING_DONE)
					.event(OrderEvent.DELIVERY_DONE)
					.action(parentStateCompleteAction())
				// **Auto transition from DELIVERING_DONE to ORDER_COMPLETED**
				.and().withExternal()
					.source(OrderState.DELIVERING)
					.target(OrderState.ORDER_COMPLETED)
					.event(OrderEvent.NEXT_STEP)
					.guard(new DeliveringDoneGuard())
					.action(
							context -> {
								System.out.println("Auto-transition from DELIVERY_DONE to ORDER_COMPLETED");
							}
					)
				.and().withExternal()
					.source(OrderState.DELIVERING_DONE)
					.target(OrderState.ORDER_COMPLETED)
					.event(OrderEvent.NEXT_STEP)
				// }}
				
		;
	}
	
	private Action<OrderState, OrderEvent> parentStateCompleteAction() {
		return context -> context.getStateMachine().sendEvent(OrderEvent.NEXT_STEP);
	}
	
	@Override
	public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
		config
			.withPersistence()
				.runtimePersister(runtimePersister);
	}
	
	@Bean
	public StateMachineService<OrderState, OrderEvent> stateMachineService(
			StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
			StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister
	) {
		return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
	}
}
