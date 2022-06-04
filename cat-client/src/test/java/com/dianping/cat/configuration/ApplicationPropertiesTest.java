package com.dianping.cat.configuration;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;

public class ApplicationPropertiesTest extends ComponentTestCase {
	@Test
	public void test() {
		ApplicationProperties ap = lookup(ApplicationProperties.class);

		Assert.assertEquals("javacat", ap.getProperty("app.name", ""));
	}
}
