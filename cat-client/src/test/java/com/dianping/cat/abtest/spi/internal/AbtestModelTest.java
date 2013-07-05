package com.dianping.cat.abtest.spi.internal;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.transform.DefaultSaxParser;

public class AbtestModelTest {
	@Test
	public void test() throws SAXException, IOException {
		InputStream in = getClass().getResourceAsStream("abtest.xml");
		AbtestModel abtest = DefaultSaxParser.parse(in);

		System.out.println(abtest);
	}
}
