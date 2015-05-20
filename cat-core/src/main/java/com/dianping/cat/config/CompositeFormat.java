package com.dianping.cat.config;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite format of many message format
 * 
 * @author renyuan.sun
 * 
 */
public class CompositeFormat extends Format {

	private AggregationMessageFormat m_aggregationMessageFormat;

	public CompositeFormat(AggregationMessageFormat amf) {
		m_aggregationMessageFormat = amf;
	}

	public AggregationMessageFormat getAmf() {
		return m_aggregationMessageFormat;
	}

	@Override
	public String parse(String input) throws ParseException {
		Object[] tokens = m_aggregationMessageFormat.getMessageFormat().parse(input);
		List<String> items = new ArrayList<String>();
		int index = 0;
		
		for (String pattern : m_aggregationMessageFormat.getFormatTokens()) {
			Format format = new DefaultFormat();
			format.setPattern(pattern);
			String output = format.parse(tokens[index].toString());
			
			items.add(output);
			index++;
		}
		return m_aggregationMessageFormat.getMessageFormat().format(items.toArray()).toString();
	}

	public void setAmf(AggregationMessageFormat amf) {
		m_aggregationMessageFormat = amf;
	}
}
