package com.dianping.cat.consumer.core.aggregation;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.dianping.cat.config.AggregationMessageFormat;
import com.dianping.cat.config.CompositeFormat;

public class CompositeFormatTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void TestParse() throws ParseException {
		AggregationMessageFormat amf = new AggregationMessageFormat("Hello {world}.I am{*}.{md5:8}.");
		CompositeFormat format = new CompositeFormat(amf);
	
		assertEquals("Hello {world}.I am Jack.{md5:8}.", format.parse("Hello world.I am Jack.balabala."));
	}
}
