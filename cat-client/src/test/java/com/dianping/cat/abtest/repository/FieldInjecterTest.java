package com.dianping.cat.abtest.repository;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.spi.internal.ABTestContextTest.MockGroupStrategy;

public class FieldInjecterTest {

	@Test
	public void testInject() throws Exception {
		MockGroupStrategy strategy = new MockGroupStrategy();

		GroupstrategyDescriptor descriptor = new GroupstrategyDescriptor();

		descriptor.getFields().add(addField("a", "String", "abc"));
		descriptor.getFields().add(addField("b", "int", "1"));
		descriptor.getFields().add(addField("c", "boolean", "true"));
		descriptor.getFields().add(addField("d", "long", "123"));
		descriptor.getFields().add(addField("e", "double", "0.01"));
		descriptor.getFields().add(addField("f", "float", "1.0f"));

		FieldInjecter inject = new FieldInjecter();

		inject.inject(strategy, descriptor);

		Assert.assertEquals("abc", strategy.getA());
		Assert.assertEquals(1, strategy.getB());
		Assert.assertEquals(true, strategy.isC());
		Assert.assertEquals(123L, strategy.getD());
		Assert.assertEquals(0.01, strategy.getE());
		Assert.assertEquals(1.0f, strategy.getF());

	}

	public Field addField(String name, String type, String value) {
		Field field = new Field();

		field.setType(type);
		field.setModifierName(name);
		field.setValue(value);

		return field;
	}

}
