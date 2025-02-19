package com.audax.entity.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.audax.state.machine.state.OrderState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StateListConverter implements AttributeConverter<Collection<OrderState>, String> {
	private static final String DELIMITER = ",";
	
	@Override
	public String convertToDatabaseColumn(Collection<OrderState> enumList) {
		if (enumList == null || enumList.isEmpty()) {
			return null;
		}
		return enumList.stream()
				.map(Enum::name)
				.collect(Collectors.joining(DELIMITER));
	}
	
	@Override
	public Collection<OrderState> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return null;
		}
		return Arrays.stream(dbData.split(DELIMITER))
				.map(OrderState::valueOf)
				.collect(Collectors.toList());
	}
}