package com.audax.state.machine.config;

import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.state.OrderState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;

@Configuration
public class JpaPersisterConfig {
	@Bean
	public JpaPersistingStateMachineInterceptor<OrderState, OrderEvent, String> stateMachineRuntimePersister(
			JpaStateMachineRepository jpaStateMachineRepository) {
		return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
	}
}
