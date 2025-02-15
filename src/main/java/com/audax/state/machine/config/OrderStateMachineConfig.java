package com.audax.state.machine.config;

import java.util.EnumSet;

import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.state.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
				.states(EnumSet.of(OrderState.NEW, OrderState.PROCESSING, OrderState.SHIPPED, OrderState.DELIVERED, OrderState.CANCELED));
	}
	
	@Override
	public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
		transitions.withExternal().source(OrderState.NEW).target(OrderState.PROCESSING).event(OrderEvent.PROCESS_ORDER)
				.and()
				.withExternal().source(OrderState.PROCESSING).target(OrderState.SHIPPED).event(OrderEvent.SHIP_ORDER)
				.and()
				.withExternal().source(OrderState.SHIPPED).target(OrderState.DELIVERED).event(OrderEvent.DELIVER_ORDER)
				.and()
				.withExternal().source(OrderState.NEW).target(OrderState.CANCELED).event(OrderEvent.CANCEL_ORDER);
	}
	
	@Override
	public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
		config.withPersistence()
				.runtimePersister(runtimePersister);
	}
	
	@Bean
	public StateMachineService<OrderState, OrderEvent> stateMachineService(
			StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
			StateMachineRuntimePersister<OrderState, OrderEvent, String> stateMachineRuntimePersister) {
		return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
	}
}
