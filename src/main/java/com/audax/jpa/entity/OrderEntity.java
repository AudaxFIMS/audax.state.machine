package com.audax.jpa.entity;

import java.util.Collection;

import com.audax.jpa.entity.converter.StateListConverter;
import com.audax.state.machine.state.OrderState;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class OrderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	@Column
	@Convert(converter = StateListConverter.class)
	private Collection<OrderState> states;
}
