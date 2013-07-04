package com.dianping.cat.home.abtest.groupstrategy;

import japa.parser.ParseException;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;

public class GroupStrategyParserTest {

	@Test
	public void test() throws IOException, ParseException {
		GroupstrategyDescriptor descriptor = GroupStrategyParser.parse(getClass().getResourceAsStream("GroupStrategyForTest"));
		
		String expectedJson = Files.forIO().readFrom(getClass().getResourceAsStream("descriptor.json"), "utf-8");
		String actualJson = GroupStrategyParser.toJson(descriptor);
		System.out.println(actualJson);
		Assert.assertEquals(expectedJson.replaceAll("\\s*", ""), actualJson.replaceAll("\\s*", ""));
		
		for(Field field : descriptor.getFields()){
			field.setValue("");
		}
		System.out.println(descriptor);
		
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("descriptor.xml"), "utf-8");
		
		Assert.assertEquals(expected.replaceAll("\\s*", ""), descriptor.toString().replaceAll("\\s*", ""));
		
	}
}
