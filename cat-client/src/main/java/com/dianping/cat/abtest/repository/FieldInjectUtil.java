package com.dianping.cat.abtest.repository;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class FieldInjectUtil {

	public void inject(ABTestGroupStrategy targetGroupStrategy, GroupstrategyDescriptor descriptor)
	      throws Exception {
		for (Field field : descriptor.getFields()) {
			java.lang.reflect.Field modifiersField = targetGroupStrategy.getClass().getDeclaredField(
			      field.getModifierName());

			modifiersField.setAccessible(true);
			
			//modifiersField.set(targetGroupStrategy, field.getValue());

			if (field.getType().equals("String")) {
				modifiersField.set(targetGroupStrategy, field.getValue());
			} else if (field.getType().equals("int") || field.getType().equals("Integer")) {
				modifiersField.setInt(targetGroupStrategy, Integer.parseInt(field.getValue()));
			} else if (field.getType().equals("boolean") || field.getType().equals("Boolean")) {
				modifiersField.setBoolean(targetGroupStrategy, Boolean.parseBoolean(field.getValue()));
			} else if (field.getType().equals("long") || field.getType().equals("Long")) {
				modifiersField.setLong(targetGroupStrategy, Long.parseLong(field.getValue()));
			} else if (field.getType().equals("double") || field.getType().equals("Double")) {
				modifiersField.setDouble(targetGroupStrategy, Double.parseDouble(field.getValue()));
			} else if (field.getType().equals("float") || field.getType().equals("Float")) {
				modifiersField.setFloat(targetGroupStrategy, Float.parseFloat(field.getValue()));
			} else {
				modifiersField.set(targetGroupStrategy, field.getValue());
			}
		}
	}

}
