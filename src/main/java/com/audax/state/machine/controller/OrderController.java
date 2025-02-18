package com.audax.state.machine.controller;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.audax.state.machine.event.OrderEvent;
import com.audax.state.machine.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
	@Autowired
	private OrderService orderService;
	
	@PostMapping
	public ResponseEntity<?> createOrder() {
		return ResponseEntity.ok(
				orderService.createOrder()
		);
	}
	
	@PostMapping("/{orderId}")
	public ResponseEntity<?> processOrder(
			@PathVariable Long orderId,
			@RequestParam OrderEvent event
	) {
		return processEvent(orderId, event);
	}
	
	private ResponseEntity<?> processEvent(Long orderId, OrderEvent event) {
		try {
			return ResponseEntity.ok(
					orderService.processOrder(orderId, event)
			);
		} catch (Throwable e) {
			return ResponseEntity
					.unprocessableEntity()
					.body(e.getMessage());
		}
	}
	
	@GetMapping(value = "/{orderId}/graphviz", produces = MediaType.IMAGE_PNG_VALUE)
	public  @ResponseBody byte[] execute(
			@PathVariable Long orderId
	) throws IOException {
		BufferedImage image = orderService.graphviz(orderId);
		
		// Convert BufferedImage to byte array
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "PNG", byteArrayOutputStream);
		byte[] imageBytes = byteArrayOutputStream.toByteArray();
		
		return imageBytes;
	}
}
