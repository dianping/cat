package com.dianping.cat.consumer.core.aggregation;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.dianping.cat.config.DefaultFormat;
import com.dianping.cat.config.Format;

public class DefaultFormatTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void TestParse() throws ParseException {
		Format format = new DefaultFormat();
		format.setPattern("*");
		assertEquals("balabala", format.parse("balabala"));
		format.setPattern("id");
		assertEquals("{id}", format.parse("balabala"));
		format.setPattern("md5:2");
		assertEquals("{md5:2}", format.parse("b2"));
		exception.expect(ParseException.class);
		format.parse("hello");
		format.parse("Ad");
	}

}
