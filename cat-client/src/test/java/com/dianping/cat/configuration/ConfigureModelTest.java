package com.dianping.cat.configuration;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.configuration.model.ClientConfigHelper;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.support.Files;

public class ConfigureModelTest {
	@Test
	public void test() throws Exception {
		InputStream in = getClass().getResourceAsStream("config.xml");
		String expected = Files.forIO().readUtf8String(in);
		ClientConfig c1 = ClientConfigHelper.fromXml(expected);
		ClientConfig c2 = ClientConfigHelper.fromXml(c1.toString());
		String actual = ClientConfigHelper.asXml(c2);

		Assert.assertEquals(c1, c2);
		Assert.assertEquals(expected.replace("\r\n", "\n"), actual.replace("\r\n", "\n"));
	}
}
