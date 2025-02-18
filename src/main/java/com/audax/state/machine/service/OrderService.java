package com.audax.state.machine.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.audax.state.machine.entity.OrderEntity;
import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.exceptions.ResourceNotFoundException;
import com.audax.state.machine.graphviz.GraphvizExporter;
import com.audax.state.machine.repository.OrderRepository;
import com.audax.state.machine.state.OrderState;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import lombok.SneakyThrows;
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
		order.setState(List.of(OrderState.NEW).toString());
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
						String.format("Event [%s] for Order: %s with state: %s- not ACCEPTABLE", event, orderId,
								stateMachine.getState().getIds())
				)
		);
	}
	
	private void handleAcceptedEvent(
			CompletableFuture<Void> future,
			StateMachine<OrderState, OrderEvent> stateMachine,
			OrderEntity order
	) {
		order.setState(stateMachine.getState().getIds().toString());
		orderRepository.save(order);
		
		future.complete(null);
	}
	
	@SneakyThrows
	public BufferedImage graphviz(Long orderId) {
		orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Lot not found"));
		
		GraphvizExporter<OrderState, OrderEvent> exporter = new GraphvizExporter<>();
		
		StateMachine<OrderState, OrderEvent> stateMachine =
				stateMachineService.acquireStateMachine(orderId.toString());
		
		String dotSrc = exporter.export(stateMachine);
		
		Parser graphParser =  new Parser();
		MutableGraph g = graphParser.read(dotSrc);

		return Graphviz.fromGraph(g).render(Format.PNG).toImage();
	}
}