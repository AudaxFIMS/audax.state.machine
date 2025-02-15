package com.audax.state.machine.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.audax.state.machine.entity.OrderEntity;
import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.repository.OrderRepository;
import com.audax.state.machine.state.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private StateMachineService<OrderState, OrderEvent> stateMachineService;
	
	public OrderEntity createOrder() {
		OrderEntity order = new OrderEntity();
		order.setState(OrderState.NEW);
		order = orderRepository.save(order);
		return order;
	}
	
	@Transactional
	public OrderEntity processOrder(Long orderId, OrderEvent event) throws Throwable {
		Optional<OrderEntity> optionalOrder = orderRepository.findById(orderId);
		if (optionalOrder.isPresent()) {
			OrderEntity order = optionalOrder.get();
			
			StateMachine<OrderState, OrderEvent> stateMachine =
					stateMachineService.acquireStateMachine(orderId.toString());
			
			CompletableFuture<Void> future = new CompletableFuture<>();
			
			stateMachine
					.sendEvent(
							Mono.just(MessageBuilder.withPayload(event).build())
					)
					.subscribe(result -> {
						if (result.getResultType() == StateMachineEventResult.ResultType.ACCEPTED) {
							handleAcceptedEvent(future, stateMachine, order);
						} else {
							handleRejectedEvent(future, stateMachine, event, orderId);
						}
					}, throwable -> {
						future.completeExceptionally(throwable);
					});
			
			stateMachineService.releaseStateMachine(orderId.toString());
			
			if (future.isCompletedExceptionally()) {
				throw future.exceptionNow();
			}
			
			return order;
		}
		throw new RuntimeException("Order not found!");
	}
	
	
	private void handleRejectedEvent(
			CompletableFuture<Void> future,
			StateMachine<OrderState, OrderEvent> stateMachine,
			OrderEvent event, Long orderId
	) {
		future.completeExceptionally(
				new IllegalStateException(
						String.format("Event [%s] for Order: %s with state: [%s]- not ACCEPTABLE", event, orderId,
								stateMachine.getState().getId())
				)
		);
	}
	
	private void handleAcceptedEvent(
			CompletableFuture<Void> future,
			StateMachine<OrderState, OrderEvent> stateMachine,
			OrderEntity order
	) {
		order.setState(stateMachine.getState().getId());
		orderRepository.save(order);
		
		future.complete(null);
	}
}