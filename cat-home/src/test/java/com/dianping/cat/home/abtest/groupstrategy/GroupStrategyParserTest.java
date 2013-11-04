package com.dianping.cat.home.abtest.groupstrategy;

import japa.parser.ParseException;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;
import com.dianping.cat.system.page.abtest.GsonBuilderManager;
import com.google.gson.Gson;

public class GroupStrategyParserTest {

	private GroupStrategyParser m_parser;

	private GroupstrategyDescriptor m_descriptorObject;

	private String m_descriptorXml;

	private String m_descriptorJson;

	@Before
	public void setup() {
		try {
			m_parser = new GroupStrategyParser();
			m_descriptorObject = m_parser.parse(getClass().getResourceAsStream("RetinaImgGroupStrategy"));
			m_descriptorXml = Files.forIO().readFrom(getClass().getResourceAsStream("descriptor.xml"), "utf-8");
			m_descriptorJson = Files.forIO().readFrom(getClass().getResourceAsStream("descriptor.json"), "utf-8");
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void given_groupstrategy_parser_can_correctly_parse() {
		assertEscapeString(m_descriptorXml, m_descriptorObject.toString());
	}

	@Test
	public void then_gsonbuilder_can_correctly_parse_json_to_object() throws IOException, ParseException {
		GsonBuilderManager manager = new GsonBuilderManager();

		Gson gson = manager.getGsonBuilder().create();

		String json = gson.toJson(m_descriptorObject, GroupstrategyDescriptor.class);

		assertEscapeString(m_descriptorJson, json);
	}

	public void assertEscapeString(String expectedString, String actualString) {
		Assert.assertEquals(expectedString.replaceAll("\r", ""), actualString.replaceAll("\r", ""));
	}
}
