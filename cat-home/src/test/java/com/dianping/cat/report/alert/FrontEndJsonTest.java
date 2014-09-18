package com.dianping.cat.report.alert;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultJsonParser;

public class FrontEndJsonTest {

	String jsonPath = "rule.json";

	@Test
	public void testJson() {
		try {
			InputStream is = this.getClass().getResourceAsStream(jsonPath);
			Rule rule = DefaultJsonParser.parse(Rule.class, is);

			Assert.assertNotNull(rule);
		} catch (Exception e) {
			Assert.assertNotNull(null);
		}
	}
}
