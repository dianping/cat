package com.dianping.cat.home.abtest.groupstrategy;

import japa.parser.ParseException;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.GroupStrategyParser.NonPrexFieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroupStrategyParserTest{
	@Test
	public void test() throws IOException, ParseException {
		GroupStrategyParser parser = new GroupStrategyParser();
		GroupstrategyDescriptor descriptor = parser.parse(getClass().getResourceAsStream("RetinaImgGroupStrategy"));
		
		GsonBuilder builder = new GsonBuilder();
		builder.setFieldNamingStrategy(new NonPrexFieldNamingStrategy());
		Gson gson = builder.create();
		String json = gson.toJson(descriptor, GroupstrategyDescriptor.class);
		System.out.println(json);
		String expectedJson = Files.forIO().readFrom(getClass().getResourceAsStream("descriptor.json"), "utf-8");
		String actualJson = gson.toJson(descriptor);
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
